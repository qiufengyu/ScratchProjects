from datetime import datetime
import MySQLdb
import jieba
import xmnlp

from bayes.bayesclass import load_bayes, test_bayes
import logging

logger = logging.getLogger(__name__)

class MySQLDB():
  def __init__(self, host, user, password, dbname):
    self.db = MySQLdb.connect(host=host, user=user, passwd=password, db=dbname, charset='utf8mb4')
    self.cursor = self.db.cursor()
    self.create_user_table = """create table user_{}
                                  (
                                    id int auto_increment primary key,
                                    `from` varchar(63) not null,
                                    `to` varchar(63) not null,
                                    subject varchar(255) null,
                                    content blob null,
                                    time datetime null,
                                    status int default 0 null
                                  ) ENGINE=InnoDB DEFAULT CHARSET= utf8mb4;
                                  """
    self.create_userdict_table = """create table userdict_{}
                                      (
                                        id int auto_increment primary key,
                                        word varchar(63) not null
                                      ) ENGINE=InnoDB DEFAULT CHARSET= utf8mb4;
                                  """
    self.select_username = """SELECT * FROM user WHERE `username` = '{}';"""
    self.insert_user = """INSERT INTO user(`username`, `nickname`, `password`) 
                          VALUES ('{}', '{}', '{}');""";
    self.select_password = """SELECT `password` FROM user WHERE `username` = '{}';"""
    self.select_status_count = """SELECT COUNT(`id`) FROM user_{} WHERE `status` = {}"""
    self.insert_mail = """INSERT INTO userlog(`from`, `to`, `subject`, `content`, `time`, `status`)
    VALUES ('{}', '{}', '{}', '{}', '{}', 0);"""
    self.insert_sender_mail = """INSERT INTO user_{}(`from`, `to`, `subject`, `content`, `time`, `status`)
    VALUES ('{}', '{}', '{}', '{}', '{}', 3);"""
    self.insert_receiver_mail = """INSERT INTO user_{}(`from`, `to`, `subject`, `content`, `time`, `status`)
        VALUES ('{}', '{}', '{}', '{}', '{}', '{}');"""
    self.select_user_mail_by_id = """SELECT * FROM user_{} WHERE `id` = {};"""
    self.select_new_mails = """SELECT * FROM userlog WHERE `to` = '{}' AND `status` = 0;"""
    self.update_userlog = """UPDATE userlog SET `status` = 1 WHERE `id` = {};"""
    self.update_user_mail_status = """UPDATE user_{} SET `status` = {} WHERE `id` = {};"""
    self.select_inbox = """SELECT * FROM user_{} WHERE `status` < 3 ORDER BY `time` DESC;"""
    self.select_outbox = """SELECT * FROM user_{} WHERE `status` = 3 ORDER BY `time` DESC;"""
    self.select_spambox = """SELECT * FROM user_{} WHERE `status` = 4 ORDER BY `time` DESC;"""
    self.select_deletebox = """SELECT * FROM user_{} WHERE `status` > 4 ORDER BY `time` DESC;"""
    self.delete_mail = """DELETE FROM user_{} WHERE `id` = {};"""
    self.select_userdict = """SELECT * FROM userdict_{};"""
    self.insert_userdict = """INSERT INTO userdict_{}(`word`) VALUES ('{}');"""
    self.delete_userdict = """DELETE FROM userdict_{} WHERE `id` = {};"""
    self.bayes_dict = load_bayes()

  def check_user(self, username, password, nickname):
    self.cursor.execute(self.select_username.format(username))
    data = self.cursor.fetchone()
    print(data)
    print(type(data))
    if data is not None:
      return False
    else:
      try:
        self.cursor.execute(self.insert_user.format(username, nickname, password))
        self.cursor.execute(self.create_user_table.format(username))
        self.cursor.execute(self.create_userdict_table.format(username))
        self.db.commit()
      except:
        self.db.rollback()
        return False
    return True

  def valid_user(self, username):
    self.cursor.execute(self.select_password.format(username))
    data = self.cursor.fetchone()
    if data and type(data) == tuple:
      return data[0]
    return None

  def get_user_mail_count(self, username, status):
    self.cursor.execute(self.select_status_count.format(username, status))
    data = self.cursor.fetchone()
    if data:
      return int(data[0])
    else:
      return 0

  def get_new_mails(self, username):
    self.cursor.execute(self.select_new_mails.format(username))
    data = self.cursor.fetchall()
    user_dict = self.get_user_dict_set(username)
    cnt = 0
    if data is not None:
      for d in data:
        cnt += 1
        self.cursor.execute(self.update_userlog.format(d[0]))
        # insert into user_username
        d_sender = str(d[1])
        d_receiver = str(d[2])
        d_subject = str(d[3])
        d_content = str(d[4], encoding="utf-8")
        d_time = d[5]
        d_time_str = d_time.strftime("%Y-%m-%d %H:%M:%S")
        status = 1
        # 使用模糊匹配，进行计算
        subject_cut = jieba.cut(d_subject)
        content_cut = jieba.cut(d_content)
        subject_correct = [xmnlp.checker(x) if len(x) > 1 else x for x in subject_cut]
        content_correct = [xmnlp.checker(x) if len(x) > 1 else x for x in content_cut]
        subject_correct = [x for x in subject_correct if x is not None]
        content_correct = [x for x in content_correct if x is not None]
        content_temp = ''.join(subject_correct) + "\n" + ''.join(content_correct)
        # 有用户词典
        if len(user_dict) > 0:
          # 模糊匹配，对邮件内容进行修正
          content_tokens = jieba.cut(content_temp)
          for x in content_tokens:
            if x in user_dict:
              status = 4
          if status == 4:
            logger.info("Mark mail from {} at {} as spam according to {}'s dict".format(d_sender, d_time_str, username))
        # 朴素贝叶斯模型分析
        r = test_bayes(self.bayes_dict, content_temp)
        if r == 1:
          status = 4
          logger.info("Mark mail from {} at {} as spam by bayes".format(d_sender, d_time_str))
        else:
          logger.info("Mark mail from {} at {} as ham by bayes".format(d_sender, d_time_str))
        self.cursor.execute(self.insert_receiver_mail.format(username, d_sender, d_receiver, d_subject, d_content, d_time_str, status))
    self.db.commit()
    return cnt

  def get_user_inbox(self, username):
    self.cursor.execute(self.select_inbox.format(username))
    data = self.cursor.fetchall()
    if data:
      return data
    else:
      return None

  def send_an_mail(self, username, receiver, subject, content):
    dt = datetime.today()
    dt_str = dt.strftime("%Y-%m-%d %H:%M:%S")
    self.cursor.execute(self.insert_mail.format(username, receiver, subject, content, dt_str))
    self.cursor.execute(self.insert_sender_mail.format(username, username, receiver, subject, content, dt_str))
    self.db.commit()

  def update_mail_status(self, username, mailid, status):
    a = self.cursor.execute(self.update_user_mail_status.format(username, status, mailid))
    self.db.commit()
    return True if a else False

  def get_user_outbox(self, username):
    self.cursor.execute(self.select_outbox.format(username))
    data = self.cursor.fetchall()
    if data:
      return data
    else:
      return None

  def get_user_spambox(self, username):
    self.cursor.execute(self.select_spambox.format(username))
    data = self.cursor.fetchall()
    if data:
      return data
    else:
      return None

  def get_user_deletebox(self, username):
    self.cursor.execute(self.select_deletebox.format(username))
    data = self.cursor.fetchall()
    if data:
      return data
    else:
      return None

  def delete_user_mail(self, username, mid):
    a = self.cursor.execute(self.delete_mail.format(username, mid))
    self.db.commit()
    return True if a else False

  def get_user_mail_by_id(self, username, mid):
    self.cursor.execute(self.select_user_mail_by_id.format(username, mid))
    data = self.cursor.fetchone()
    if data:
      return data
    else:
      return None

  def get_user_dict_list(self, username):
    self.cursor.execute(self.select_userdict.format(username))
    data = self.cursor.fetchall()
    userdict = []
    if data:
      for d in data:
        dd = {}
        dd["id"] = int(d[0])
        dd["word"] = str(d[1])
        userdict.append(dd)
    return userdict

  def get_user_dict_set(self, username):
    self.cursor.execute(self.select_userdict.format(username))
    data = self.cursor.fetchall()
    userdictset = set()
    if data:
      for d in data:
        userdictset.add(str(d[1]))
    return userdictset

  def delete_user_dict(self, username, wordid):
    a = self.cursor.execute(self.delete_userdict.format(username, wordid))
    self.db.commit()
    return True if a else False

  def insert_user_dict(self, username, word):
    a = self.cursor.execute(self.insert_userdict.format(username, word))
    self.db.commit()
    return True if a else False


