package lab.mops.ai.domain.conversation;

import nz.geek.jack.libs.ddd.domain.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends BaseRepository<Conversation, ConversationId> {}
