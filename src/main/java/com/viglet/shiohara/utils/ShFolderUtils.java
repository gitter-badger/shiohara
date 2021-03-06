package com.viglet.shiohara.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.viglet.shiohara.object.ShObjectType;
import com.viglet.shiohara.persistence.model.folder.ShFolder;
import com.viglet.shiohara.persistence.model.globalid.ShGlobalId;
import com.viglet.shiohara.persistence.model.post.ShPost;
import com.viglet.shiohara.persistence.model.post.ShPostAttr;
import com.viglet.shiohara.persistence.model.reference.ShReference;
import com.viglet.shiohara.persistence.model.site.ShSite;
import com.viglet.shiohara.persistence.repository.folder.ShFolderRepository;
import com.viglet.shiohara.persistence.repository.globalid.ShGlobalIdRepository;
import com.viglet.shiohara.persistence.repository.post.ShPostAttrRepository;
import com.viglet.shiohara.persistence.repository.post.ShPostRepository;
import com.viglet.shiohara.persistence.repository.reference.ShReferenceRepository;
import com.viglet.shiohara.url.ShURLScheme;

@Component
public class ShFolderUtils {
	@Autowired
	private ShFolderRepository shFolderRepository;
	@Autowired
	private ShPostRepository shPostRepository;
	@Autowired
	private ShPostAttrRepository shPostAttrRepository;
	@Autowired
	private ShGlobalIdRepository shGlobalIdRepository;
	@Autowired
	private ShReferenceRepository shReferenceRepository;
	
	@Autowired
	private ShURLScheme shURLScheme;

	public JSONObject toJSON(ShFolder shFolder) {
		JSONObject shFolderItemAttrs = new JSONObject();

		JSONObject shFolderItemSystemAttrs = new JSONObject();
		shFolderItemSystemAttrs.put("id", shFolder.getId());
		shFolderItemSystemAttrs.put("title", shFolder.getName());
		shFolderItemSystemAttrs.put("link", this.folderPath(shFolder));

		shFolderItemAttrs.put("system", shFolderItemSystemAttrs);

		return shFolderItemAttrs;
	}

	public ArrayList<ShFolder> breadcrumb(ShFolder shFolder) {
		if (shFolder != null) {
			boolean rootFolder = false;
			ArrayList<ShFolder> folderBreadcrumb = new ArrayList<ShFolder>();
			folderBreadcrumb.add(shFolder);
			ShFolder parentFolder = shFolder.getParentFolder();
			while (parentFolder != null && !rootFolder) {
				folderBreadcrumb.add(parentFolder);
				if ((parentFolder.getRootFolder() == (byte) 1) || (parentFolder.getParentFolder() == null)) {
					rootFolder = true;
				} else {
					parentFolder = parentFolder.getParentFolder();
				}
			}

			Collections.reverse(folderBreadcrumb);
			return folderBreadcrumb;
		} else {
			return null;
		}
	}

	public String folderPath(ShFolder shFolder) {
		return this.folderPath(shFolder, "/");
	}

	public String folderPath(ShFolder shFolder, String separator) {
		if (shFolder != null) {
			boolean rootFolder = false;
			ArrayList<String> pathContexts = new ArrayList<String>();
			if (!(shFolder.getFurl().equals("home") && shFolder.getRootFolder() == (byte) 1)) {
				pathContexts.add(shFolder.getFurl());
			}
			ShFolder parentFolder = shFolder.getParentFolder();
			while (parentFolder != null && !rootFolder) {

				if ((parentFolder.getRootFolder() == (byte) 1) || (parentFolder.getParentFolder() == null)) {
					rootFolder = true;
					if (!parentFolder.getName().toLowerCase().equals("home")) {
						pathContexts.add(parentFolder.getFurl());
					}
				} else {
					pathContexts.add(parentFolder.getFurl());
					parentFolder = parentFolder.getParentFolder();
				}
			}

			String path = "";

			for (String context : pathContexts) {
				path = context + separator + path;
			}
			path = separator + path;
			return path;
		} else {
			return separator;
		}

	}

	public String directoryPath(ShFolder shFolder, String separator) {
		if (shFolder != null) {
			boolean rootFolder = false;
			ArrayList<String> pathContexts = new ArrayList<String>();
			pathContexts.add(shFolder.getName());
			ShFolder parentFolder = shFolder.getParentFolder();
			while (parentFolder != null && !rootFolder) {
				pathContexts.add(parentFolder.getName());
				if ((parentFolder.getRootFolder() == (byte) 1) || (parentFolder.getParentFolder() == null)) {
					rootFolder = true;
				} else {
					parentFolder = parentFolder.getParentFolder();
				}
			}
			
			String path = "";

			for (String context : pathContexts) {
				path = context + separator + path;
			}
			path = separator + path;
			return path;
		} else {
			return separator;
		}

	}

	public ShSite getSite(ShFolder shFolder) {
		ShSite shSite = null;
		if (shFolder != null) {
			boolean rootFolder = false;
			if ((shFolder.getRootFolder() == (byte) 1) || (shFolder.getParentFolder() == null)) {
				shSite = shFolder.getShSite();
			} else {
				ShFolder parentFolder = shFolder.getParentFolder();
				while (parentFolder != null && !rootFolder) {
					if ((parentFolder.getRootFolder() == (byte) 1) || (parentFolder.getParentFolder() == null)) {
						rootFolder = true;
						shSite = parentFolder.getShSite();
					} else {
						parentFolder = parentFolder.getParentFolder();
					}
				}
			}
		}
		return shSite;
	}

	public ShFolder folderFromPath(ShSite shSite, String folderPath) {
		return this.folderFromPath(shSite, folderPath, "/");
	}

	public ShFolder folderFromPath(ShSite shSite, String folderPath, String separator) {
		ShFolder currentFolder = null;
		String[] contexts = folderPath.split(separator);
		if (contexts.length == 0) {
			// Root Folder (Home)
			currentFolder = shFolderRepository.findByShSiteAndFurl(shSite, "home");
		} else {
			for (int i = 1; i < contexts.length; i++) {
				if (i == 1) {
					// When is null folder, because is rootFolder and it contains shSite attribute
					currentFolder = shFolderRepository.findByShSiteAndFurl(shSite, contexts[i]);
					if (currentFolder == null) {

						// Is not Root Folder, will try use the Home Folder
						currentFolder = shFolderRepository.findByShSiteAndFurl(shSite, "home");
						// Now will try access the first Folder non Root
						currentFolder = shFolderRepository.findByParentFolderAndFurl(currentFolder, contexts[i]);
					}
				} else {
					currentFolder = shFolderRepository.findByParentFolderAndFurl(currentFolder, contexts[i]);
				}

			}
		}
		return currentFolder;
	}

	public String generateFolderLink(ShFolder shFolder) {
		String link = shURLScheme.get(shFolder).toString();
		link = link + this.folderPath(shFolder);
		return link;
	}

	public String generateFolderLinkById(String folderID) {
		ShFolder shFolder = shFolderRepository.findById(UUID.fromString(folderID)).get();
		return this.generateFolderLink(shFolder);
	}

	@Transactional
	public boolean deleteFolder(ShFolder shFolder) {

		for (ShPost shPost : shPostRepository.findByShFolder(shFolder)) {
			// TODO: Check relation and show to user decides.
			List<ShReference> shGlobalFromId = shReferenceRepository.findByShGlobalFromId(shPost.getShGlobalId());			
			List<ShReference> shGlobalToId = shReferenceRepository.findByShGlobalToId(shPost.getShGlobalId());
			shReferenceRepository.deleteInBatch(shGlobalFromId);
			shReferenceRepository.deleteInBatch(shGlobalToId);
		}
		
		for (ShPost shPost : shPostRepository.findByShFolder(shFolder)) {
			Set<ShPostAttr> shPostAttrs = shPostAttrRepository.findByShPost(shPost);
			
			shPostAttrRepository.deleteInBatch(shPostAttrs);
			shGlobalIdRepository.delete(shPost.getShGlobalId().getId());
		}
		
		shPostRepository.deleteInBatch(shPostRepository.findByShFolder(shFolder));

		for (ShFolder shFolderChild : shFolderRepository.findByParentFolder(shFolder)) {
			this.deleteFolder(shFolderChild);
		}
		shGlobalIdRepository.delete(shFolder.getShGlobalId().getId());
		shFolderRepository.delete(shFolder.getId());
		return true;
	}

	public ShFolder copy(ShFolder shFolder, ShGlobalId shGlobalIdDest) {
		// TODO: Copy objects into Folder
		ShFolder shFolderCopy = new ShFolder();
		if (shGlobalIdDest.getType().equals(ShObjectType.FOLDER)) {
			ShFolder shFolderDest = (ShFolder) shGlobalIdDest.getShObject();
			shFolderCopy.setParentFolder(shFolderDest);
			shFolderCopy.setShSite(null);
			shFolderCopy.setRootFolder((byte) 0);
		} else if (shGlobalIdDest.getType().equals(ShObjectType.SITE)) {
			ShSite shSiteDest = (ShSite) shGlobalIdDest.getShObject();
			shFolderCopy.setParentFolder(null);
			shFolderCopy.setShSite(shSiteDest);
			shFolderCopy.setRootFolder((byte) 1);
		} else {
			return null;
		}
		shFolderCopy.setDate(new Date());
		shFolderCopy.setName(shFolder.getName());

		shFolderRepository.save(shFolderCopy);

		ShGlobalId shGlobalId = new ShGlobalId();
		shGlobalId.setShObject(shFolderCopy);
		shGlobalId.setType(ShObjectType.FOLDER);

		shGlobalIdRepository.saveAndFlush(shGlobalId);
		shFolderCopy.setShGlobalId(shGlobalId);

		return shFolderCopy;
	}

}
