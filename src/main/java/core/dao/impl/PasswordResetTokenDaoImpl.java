package core.dao.impl;

import core.model.Account;
import core.model.PasswordResetToken;
import core.dao.PasswordResetTokenDao;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Adrian on 29/06/2015.
 */
@Repository
@Transactional
public class PasswordResetTokenDaoImpl implements PasswordResetTokenDao {

    /*@PersistenceContext
    private EntityManager emgr;*/

    @Autowired
    SessionFactory sessionFactory;

    @Override
    public PasswordResetToken createPasswordResetToken(PasswordResetToken token) {


        PasswordResetToken passwordResetToken = findCurrentPasswordResetTokenOfAccount(token.getAcc());
        if (passwordResetToken == null) {
            sessionFactory.getCurrentSession()
                    .saveOrUpdate(token);
            sessionFactory.getCurrentSession()
                    .flush();
            return token;
        } else {
            System.out.println("Updating instead of creating passwordResetToken");
            PasswordResetToken updatedToken = updatePasswordResetToken(token, token.getAcc());
            return updatedToken;
        }
    }

    @Override
    public PasswordResetToken findPasswordResetToken(String token) {
      /*  Query query = emgr.createQuery("SELECT v FROM password_reset_token v WHERE v.token = :token");
        query.setParameter("token", token);
        if (!query.getResultList().isEmpty())
            return (PasswordResetToken) query.getResultList().get(0);
        else
            return null;*/
        PasswordResetToken passwordResetToken = (PasswordResetToken) sessionFactory.getCurrentSession()
                .createCriteria(PasswordResetToken.class)
                .add(Restrictions.eq("token", token))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).uniqueResult();
        return passwordResetToken;
    }



    @Override
    public PasswordResetToken findCurrentPasswordResetTokenOfAccount(Account acc) {
        if (acc == null)
            return null;
        PasswordResetToken currentPasswordResetToken = (PasswordResetToken) sessionFactory.getCurrentSession()
                .createCriteria(PasswordResetToken.class)
                .add(Restrictions.eq("acc", acc))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).uniqueResult();
        return currentPasswordResetToken;
    }

    @Override
    public PasswordResetToken updatePasswordResetToken(PasswordResetToken newToken, Account acc) {
        PasswordResetToken tokenToBeUpdated = (PasswordResetToken) sessionFactory.getCurrentSession()
                .createCriteria(PasswordResetToken.class)
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
