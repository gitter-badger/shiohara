package com.viglet.shiohara.persistence.repository.history;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.viglet.shiohara.persistence.model.history.ShHistory;

public interface ShHistoryRepository extends JpaRepository<ShHistory, UUID> {

	List<ShHistory> findAll();

	Set<ShHistory> findByShObject(UUID shObject);
	
	Set<ShHistory> findByShSite(UUID shSite);
	
	Optional<ShHistory> findById(UUID id);
	
	@SuppressWarnings("unchecked")
	ShHistory save(ShHistory shHistory);

	@Modifying
	@Query("delete from ShHistory h where h.id = ?1")
	void delete(UUID id);
}
