package com.viglet.shiohara.persistence.model.folder;

import javax.persistence.*;

import org.hibernate.annotations.Fetch;
import org.hibernate.search.annotations.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.viglet.shiohara.persistence.model.object.ShObject;
import com.viglet.shiohara.persistence.model.post.ShPost;
import com.viglet.shiohara.persistence.model.site.ShSite;

import java.util.List;

/**
 * The persistent class for the ShPost database table.
 * 
 */
@Entity
@NamedQuery(name = "ShFolder.findAll", query = "SELECT c FROM ShFolder c")
@JsonIgnoreProperties({ "shFolders", "shPosts" })
public class ShFolder extends ShObject {
	private static final long serialVersionUID = 1L;

	@Field
	private String name;

	private byte rootFolder;

	// bi-directional many-to-one association to ShFolder
	@ManyToOne
	@JoinColumn(name = "parent_folder_id")
	private ShFolder parentFolder;

	// bi-directional many-to-one association to ShSite
	@ManyToOne
	@JoinColumn(name = "site_id")
	private ShSite shSite;

	// bi-directional many-to-one association to ShFolder
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parentFolder", cascade = CascadeType.ALL)
	@Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
	private List<ShFolder> shFolders;

	// bi-directional many-to-one association to ShFolder
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "shFolder", cascade = CascadeType.ALL)
	@Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
	private List<ShPost> shPosts;

	public ShFolder() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ShFolder getParentFolder() {
		return parentFolder;
	}

	public void setParentFolder(ShFolder parentFolder) {
		this.parentFolder = parentFolder;
	}

	public ShSite getShSite() {
		return shSite;
	}

	public void setShSite(ShSite shSite) {
		this.shSite = shSite;
	}

	public List<ShFolder> getShFolders() {
		return this.shFolders;
	}

	public void setShFolders(List<ShFolder> shFolders) {
		this.shFolders = shFolders;
	}

	public List<ShPost> getShPosts() {
		return this.shPosts;
	}

	public void setShPosts(List<ShPost> shPosts) {
		this.shPosts = shPosts;
	}

	public byte getRootFolder() {
		return rootFolder;
	}

	public void setRootFolder(byte rootFolder) {
		this.rootFolder = rootFolder;
	}

}
