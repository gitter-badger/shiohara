package com.viglet.shiohara.persistence.model.site;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.Fetch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.viglet.shiohara.persistence.model.channel.ShChannel;
import com.viglet.shiohara.persistence.model.post.ShPost;

/**
 * The persistent class for the ShSite database table.
 * 
 */
@Entity
@NamedQuery(name = "ShSite.findAll", query = "SELECT s FROM ShSite s")
@JsonIgnoreProperties({ "shChannels", "shPosts" })
public class ShSite implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String name;

	private String description;

	private String url;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "shSite", cascade = CascadeType.ALL)
	@Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
	private List<ShChannel> shChannels;

	public ShSite() {
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<ShChannel> getShChannels() {
		return this.shChannels;
	}

	public void setShChannels(List<ShChannel> shChannels) {
		this.shChannels = shChannels;
	}

}