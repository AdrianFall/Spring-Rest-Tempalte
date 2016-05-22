package core.dao.impl;

import core.model.Account;
import core.model.VerificationToken;
import core.dao.VerificationTokenDao;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Adrian on 14/05/2015.
 */
@Repository
@Transactional
public class VerificationTokenDaoImpl implements VerificationTokenDao {

    /*@PersistenceContext
    private EntityManager emgr;*/

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public VerificationToken createVerificationToken(VerificationToken token) {
        /*emgr.persist(token);
        emgr.flush();
        return token;*/
        // Ensure the account doesn't already have a token associated with its acc
        VerificationToken verificationToken = findCurrentVerificationTokenOfAccount(token.getAcc());
        System.out.println("createVerificationToken");
        if (verificationToken == null) {
            sessionFactory.getCurrentSession()
                    .saveOrUpdate(token);
            sessionFactory.getCurrentSession()
                    .flush();
            return token;
        } else {
            System.out.println("Updating instead of creating account");
            VerificationToken updatedToken = updateVerificationToken(token, token.getAcc());
            return updatedToken;
        }

    }

    @Override
    public VerificationToken findVerificationToken(String verificationToken) {
        /*Query query = emgr.createQuery("SELECT v FROM verification_token v WHERE v.token = :token");
        query.setParameter("token", verificationToken);
        if (!query.getResultList().isEmpty())
            return (VerificationToken) query.getResultList().get(0);
        else
            return null;*/

        VerificationToken verToken = (VerificationToken) sessionFactory.getCurrentSession()
                .createCriteria(VerificationToken.class)
                .add(Restrictions.eq("token", verificationToken))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).uniqueResult();
        return verToken;
    }

    @Override
    public VerificationToken findCurrentVerificationTokenOfAccount(Account acc) {
        if (acc == null)
            return null;
        VerificationToken currentVerificationToken = (VerificationToken) sessionFactory.getCurrentSession()
                .createCriteria(VerificationToken.class)
                .add(Restrictions.eq("acc", acc))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).uniqueResult();
        return currentVerificationToken;
    }

    @Override
    public VerificationToken updateVerificationToken(VerificationToken newToken, Account acc) {

        VerificationToken tokenToBeUpdated = (VerificationToken) sessionFactory.getCurrentSession()
                .createCriteria(VerificationToken.class)
                .add(Restrictions.eq("acc", acc))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).uniqueResult();

        if (tokenToBeUpdated != null) {
            System.out.println("Obtained tokenToBeUpdated");
            tokenToBeUpdated.setToken(newToken.getToken());
            tokenToBeUpdated.setExpiryDate(newToken.getExpiryDate());
            sessionFactory.getCurrentSession()
                    .saveOrUpdate(tokenToBeUpdated);
            sessionFactory.getCurrentSession()
                    .flush();
        }

        return tokenToBeUpdated;
    }
}
