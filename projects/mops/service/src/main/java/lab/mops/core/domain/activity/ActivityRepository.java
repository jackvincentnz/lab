package lab.mops.core.domain.activity;

import lab.libs.ddd.domain.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends BaseRepository<Activity, ActivityId> {}
