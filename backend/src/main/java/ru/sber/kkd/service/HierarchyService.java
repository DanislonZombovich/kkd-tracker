package ru.sber.kkd.service;

import org.springframework.stereotype.Service;
import ru.sber.kkd.entity.OrganizationalUnit;
import ru.sber.kkd.repository.OrganizationalUnitRepository;

import java.util.*;

@Service
public class HierarchyService {
    private final OrganizationalUnitRepository unitRepository;

    public HierarchyService(OrganizationalUnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    public Set<String> getDescendantsOrSelf(String rootUnitId) {
        List<OrganizationalUnit> allUnits = unitRepository.findAll();
        Map<String, List<String>> childrenMap = new HashMap<>();
        for (OrganizationalUnit u : allUnits) {
            if (u.getParentId() != null) {
                childrenMap.computeIfAbsent(u.getParentId(), k -> new ArrayList<>()).add(u.getId());
            }
        }

        Set<String> result = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        queue.add(rootUnitId);
        result.add(rootUnitId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            List<String> children = childrenMap.getOrDefault(current, List.of());
            for (String child : children) {
                if (result.add(child)) {
                    queue.add(child);
                }
            }
        }
        return result;
    }

    public List<Map<String, String>> getUnitPath(String unitId) {
        List<OrganizationalUnit> allUnits = unitRepository.findAll();
        Map<String, OrganizationalUnit> map = new HashMap<>();
        for (OrganizationalUnit u : allUnits) {
            map.put(u.getId(), u);
        }

        List<Map<String, String>> path = new ArrayList<>();
        String current = unitId;
        while (current != null && map.containsKey(current)) {
            OrganizationalUnit u = map.get(current);
            path.add(Map.of("id", u.getId(), "name", u.getName()));
            current = u.getParentId();
        }
        Collections.reverse(path);
        return path;
    }
}
