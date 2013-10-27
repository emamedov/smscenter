package org.eminmamedov.smscenter.services;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.dao.SmsMapper;
import org.eminmamedov.smscenter.dao.UserMapper;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.datamodel.Sender;
import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.exceptions.NotFoundException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class SmsCenterService implements InitializingBean {

    private static final Logger log = Logger.getLogger(SmsCenterService.class);

    @Autowired
    private SmsMapper smsMapper;
    @Autowired
    private UserMapper userMapper;
    @Value("${sender.allsenders}")
    private String allSendersSign;
    private AtomicLong lastId;

    @Override
    public void afterPropertiesSet() {
        Integer lastSmsId = smsMapper.getLastSmsId();
        this.lastId = new AtomicLong(lastSmsId == null ? 0 : lastSmsId);
        log.debug("Last ID has been initialized: " + lastId.get());
    }

    public Long reserveServerId() {
        return this.lastId.incrementAndGet();
    }

    public void addMessages(List<SMSMessage> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        this.smsMapper.addMessages(messages);
    }

    public List<SMSMessage> getMessages(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return this.smsMapper.getMessages(ids);
    }

    public User getUser(String login, String password, String ip) {
        if (StringUtils.isBlank(login) || StringUtils.isBlank(password)) {
            throw new NotFoundException("Login and Password should be specified");
        }
        User foundUser = null;
        try {
            foundUser = userMapper.getUser(login, DigestUtils.sha512Hex(password));
            if (foundUser == null) {
                throw new NotFoundException("Wrong login or password. USER = " + login);
            }
            if ((foundUser.getAllowedIp() != null) && !StringUtils.equals(ip, foundUser.getAllowedIp())) {
                throw new NotFoundException("User doesn't allow to connect from IP " + ip);
            }
            if (foundUser.isEnabled()) {
                foundUser.setLastLogin(new Date());
                userMapper.update(foundUser);
            }
        } catch (NotFoundException e) {
            log.debug("Wrong login or password. USER = " + login);
            userMapper.wrongPassword(login);
            throw e;
        }
        return foundUser;
    }

    public Sender getSender(User user, String senderSign) {
        Sender sender = null;
        if (senderSign == null) {
            for (Sender tempSender : user.getSenders()) {
                if (!allSendersSign.equals(tempSender.getSign())) {
                    sender = tempSender;
                    break;
                }
            }
        } else {
            sender = findSender(senderSign, user.getSenders());
            if (sender == null) {
                sender = findSender(allSendersSign, user.getSenders());
            }
        }
        log.debug("Sender " + sender + " has been found for user " + user + " by senderSign " + senderSign);
        return sender;
    }

    private Sender findSender(String senderSign, List<Sender> senders) {
        Sender sender = null;
        for (Sender tempSender : senders) {
            if (senderSign.equals(tempSender.getSign())) {
                sender = tempSender;
                break;
            }
        }
        return sender;
    }

    public List<SMSMessage> getUpdatedMessages(User user, int count) {
        return smsMapper.getUpdatedMessages(user, count);
    }

    public void setInformedFlagForMessage(Long id, boolean informed) {
        smsMapper.setInformed(id, informed);
    }

    public void setSmsMapper(SmsMapper smsMapper) {
        this.smsMapper = smsMapper;
    }

    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public void setAllSendersSign(String allSendersSign) {
        this.allSendersSign = allSendersSign;
    }

}
