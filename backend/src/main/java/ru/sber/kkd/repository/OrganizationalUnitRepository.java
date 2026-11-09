package ru.sber.kkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.kkd.entity.OrganizationalUnit;

import java.util.List;

@Repository
public interface OrganizationalUnitRepository extends JpaRepository<OrganizationalUnit, String> {
    List<OrganizationalUnit> findByParentId(String parentId);
}
