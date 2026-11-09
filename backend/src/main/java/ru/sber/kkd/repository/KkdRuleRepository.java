package ru.sber.kkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.kkd.entity.KkdRuleEntity;

@Repository
public interface KkdRuleRepository extends JpaRepository<KkdRuleEntity, String> {
}
