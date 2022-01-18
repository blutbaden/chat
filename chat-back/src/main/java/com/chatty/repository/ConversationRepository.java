package com.chatty.repository;

import com.chatty.domain.Conversation;
import com.chatty.domain.User;
import com.chatty.domain.enumeration.ConversationState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data SQL repository for the Conversation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("select c from Conversation c where c.room.id =:id ORDER BY c.createdDate desc ")
    Page<Conversation> findAllByRoomId(Pageable pageable, @Param("id") Long roomId);
    @Query("select c.sender.id as sender, COUNT(c) as count FROM Conversation c where c.conversationState =:state and c.receiver.id =:recId and c.room.group is null GROUP BY c.sender.id")
    List<?> getConversationsCountByReceiverAndStateGroupedBySender(@Param("state") ConversationState state, @Param("recId") Long recId);
    @Query("select c.room.id as room, COUNT(c) as count FROM Conversation c where c.conversationState =:state and c.receiver.id =:recId GROUP BY c.room.id")
    List<?> getConversationsCountByReceiverAndStateGroupedByRoom(@Param("state") ConversationState state, @Param("recId") Long recId);
    @Query("select COUNT(c) as count FROM Conversation c where c.conversationState =:state and c.receiver.id =:recId and c.room.id =:roomId")
    int getConversationCountByRoomAndReceiverAndState(@Param("state") ConversationState state, @Param("recId") Long recId, @Param("roomId") Long roomId );
    @Modifying
    @Query("update Conversation c set c.conversationState =:state where c.room.id = :roomId and c.receiver.id =:recId")
    void updateConversationStateByRoom(@Param(value = "state") ConversationState state, @Param(value = "roomId") Long roomId, @Param(value = "recId") Long recId);
}
