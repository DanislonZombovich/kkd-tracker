package ru.sber.kkd.service;

import org.springframework.stereotype.Service;
import ru.sber.kkd.dto.DeltaSummaryDto;
import ru.sber.kkd.entity.KkdIssueEntity;

import java.util.*;

@Service
public class DeltaService {

    public DeltaSummaryDto calculateDelta(
            List<KkdIssueEntity> currentIssues,
            List<KkdIssueEntity> previousIssues,
            boolean hasComparison
    ) {
        if (!hasComparison || previousIssues == null) {
            return DeltaSummaryDto.builder()
                    .newCount(null)
                    .persisting(null)
                    .resolved(null)
                    .build();
        }

        Set<String> curFingerprints = new HashSet<>();
        for (KkdIssueEntity i : currentIssues) {
            // Ignore resolved markers if already stored
            if (!"resolved".equalsIgnoreCase(i.getDeltaStatus())) {
                curFingerprints.add(i.getFingerprint());
            }
        }

        Set<String> prevFingerprints = new HashSet<>();
        for (KkdIssueEntity i : previousIssues) {
            if (!"resolved".equalsIgnoreCase(i.getDeltaStatus())) {
                prevFingerprints.add(i.getFingerprint());
            }
        }

        int newCount = 0;
        int persistingCount = 0;
        for (String fp : curFingerprints) {
            if (prevFingerprints.contains(fp)) {
                persistingCount++;
            } else {
                newCount++;
            }
        }

        int resolvedCount = 0;
        for (String fp : prevFingerprints) {
            if (!curFingerprints.contains(fp)) {
                resolvedCount++;
            }
        }

        return DeltaSummaryDto.builder()
                .newCount(newCount)
                .persisting(persistingCount)
                .resolved(resolvedCount)
                .build();
    }
}
