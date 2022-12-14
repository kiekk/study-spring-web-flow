package com.example.studyspringwebflow.repository;

import com.example.studyspringwebflow.entity.Account;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository("accountRepository")
public class JpaAccountRepository implements AccountRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Account findByUsername(String username) {
        String hql = "select c from Account c where c.username=:username";
        TypedQuery<Account> query = this.entityManager.createQuery(hql, Account.class).setParameter("username",
                username);
        List<Account> accounts = query.getResultList();

        return accounts.size() == 1 ? accounts.get(0) : null;
    }

    @Override
    public Account findById(long id) {
        return this.entityManager.find(Account.class, id);
    }

    @Override
    public Account save(Account account) {
        if (account.getId() != null) {
            return this.entityManager.merge(account);
        } else {
            this.entityManager.persist(account);
            return account;
        }
    }

}
