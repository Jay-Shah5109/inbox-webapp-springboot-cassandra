package io.javabrains.inbox.folders;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnreadEmailStatsRepository extends CassandraRepository<UnreadEmailStats, String> {

    List<UnreadEmailStats> findAllById(String id);

    @Query("update unread_email_stats set unreadCount=unreadCount+1 where userID=?0 and label=?1")
    public void incrementCount(String userID, String label);
    @Query("update unread_email_stats set unreadCount=unreadCount-1 where userID=?0 and label=?1")
    public void decrementCount(String userID, String label);
}
