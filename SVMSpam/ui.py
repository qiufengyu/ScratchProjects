import os
import sys
import form

from PyQt5.QtCore import *
from PyQt5.QtWidgets import *

from svm import train_only, test_only


class SpamUI(QMainWindow, form.Ui_MainWindow):
  def __init__(self):
    super().__init__()
    # 加载 UI
    self.setupUi(self)
    # 初始化
    self.content.clear()
    # 注册一些事件的监听
    self.analyzeButton.clicked.connect(self.analyze)
    self.clearButton.clicked.connect(self.clear)
    self.trainButton.clicked.connect(self.train)

  @pyqtSlot()
  def clear(self):
    # QApplication.processEvents()
    self.content.clear()
    self.statusBar().showMessage("")

  @pyqtSlot()
  def train(self):
    # QApplication.processEvents()
    errorRate = train_only()
    self.statusBar().showMessage("测试集错误率：{}".format(errorRate))

  @pyqtSlot()
  def analyze(self):
    # QApplication.processEvents()
    raw_text = self.content.toPlainText().strip()
    lines = [line.strip() for line in raw_text.split("\n")]
    empty_input = True
    for l in lines:
      if len(l) >= 1:
        empty_input = False
    if empty_input:
      self.statusBar().showMessage("文本内容为空，请重新输入！")
    else:
      res = test_only(lines)
      if res == 1:
        self.statusBar().showMessage("垃圾邮件！")
      elif res == -1:
        self.statusBar().showMessage("正常邮件！")
      elif res == 0:
        self.statusBar().showMessage("未找到模型，请重新训练！")


if __name__ == "__main__":
  app = QApplication(sys.argv)
  ui = SpamUI()
  ui.show()
  sys.exit(app.exec_())
