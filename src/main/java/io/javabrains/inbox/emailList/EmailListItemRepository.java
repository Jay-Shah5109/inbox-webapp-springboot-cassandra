package io.javabrains.inbox.emailList;

import io.javabrains.inbox.email.Email;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailListItemRepository extends CassandraRepository<EmailListItem, EmailListItemKey> {

//    List<EmailListItem> findAllById(EmailListItemKey id);

    List<EmailListItem> findAllByKey_IdAndKey_Label(String id, String label);
}
