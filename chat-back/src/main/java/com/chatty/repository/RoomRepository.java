package com.chatty.repository;

import com.chatty.domain.Group;
import com.chatty.domain.Room;
import com.chatty.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data SQL repository for the Room entity.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query(
        value = "select distinct room from Room room left join fetch room.users",
        countQuery = "select count(distinct room) from Room room"
    )
    Page<Room> findAllWithEagerRelationships(Pageable pageable);

    @Query("select distinct room from Room room left join fetch room.users")
    List<Room> findAllWithEagerRelationships();

    @Query("select room from Room room left join fetch room.users where room.id =:id")
    Optional<Room> findOneWithEagerRelationships(@Param("id") Long id);

    List<Room> findAllByGroup(Group group);

    List<Room> findAllByUsersIsContaining(User user);

    List<Room> findAllByUsersIsContainingAndGroupNotNull(User user);

    List<Room> findAllByUsersIsContainingAndGroup(User user, Group group);

    Optional<Room> findFirstByUsersInAndGroupIsNull(Set<User> user);
}
