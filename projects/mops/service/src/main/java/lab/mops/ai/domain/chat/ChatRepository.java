package lab.mops.ai.domain.chat;

import nz.geek.jack.libs.ddd.domain.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends BaseRepository<Chat, ChatId> {}
