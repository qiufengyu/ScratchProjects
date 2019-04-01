# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'form.ui'
#
# Created by: PyQt5 UI code generator 5.11.3
#
# WARNING! All changes made in this file will be lost!

from PyQt5 import QtCore, QtGui, QtWidgets

class Ui_MainWindow(object):
    def setupUi(self, MainWindow):
        MainWindow.setObjectName("Spam")
        MainWindow.resize(400, 330)
        font = QtGui.QFont()
        font.setFamily("微软雅黑")
        font.setPointSize(10)
        font.setBold(False)
        font.setItalic(False)
        font.setWeight(50)
        MainWindow.setFont(font)
        self.verticalLayoutWidget = QtWidgets.QWidget(MainWindow)
        self.verticalLayoutWidget.setGeometry(QtCore.QRect(10, 10, 381, 281))
        self.verticalLayoutWidget.setObjectName("verticalLayoutWidget")
        self.verticalLayout = QtWidgets.QVBoxLayout(self.verticalLayoutWidget)
        self.verticalLayout.setContentsMargins(0, 0, 0, 0)
        self.verticalLayout.setObjectName("verticalLayout")
        self.label = QtWidgets.QLabel(self.verticalLayoutWidget)
        self.label.setObjectName("label")
        self.verticalLayout.addWidget(self.label)
        self.content = QtWidgets.QTextEdit(self.verticalLayoutWidget)
        self.content.setObjectName("content")
        self.verticalLayout.addWidget(self.content)
        self.gridLayout = QtWidgets.QGridLayout()
        self.gridLayout.setObjectName("gridLayout")
        self.analyzeButton = QtWidgets.QPushButton(self.verticalLayoutWidget)
        self.analyzeButton.setObjectName("analyzeButton")
        self.gridLayout.addWidget(self.analyzeButton, 1, 0, 1, 1)
        self.trainButton = QtWidgets.QPushButton(self.verticalLayoutWidget)
        self.trainButton.setObjectName("trainButton")
        self.gridLayout.addWidget(self.trainButton, 1, 2, 1, 1)
        self.clearButton = QtWidgets.QPushButton(self.verticalLayoutWidget)
        self.clearButton.setObjectName("clearButton")
        self.gridLayout.addWidget(self.clearButton, 1, 1, 1, 1)
        self.verticalLayout.addLayout(self.gridLayout)

        self.retranslateUi(MainWindow)
        QtCore.QMetaObject.connectSlotsByName(MainWindow)

    def retranslateUi(self, MainWindow):
        _translate = QtCore.QCoreApplication.translate
        MainWindow.setWindowTitle(_translate("Spam", "垃圾邮件分析软件"))
        self.label.setText(_translate("Spam", "邮件内容"))
        self.analyzeButton.setText(_translate("Spam", "分析"))
        self.trainButton.setText(_translate("Spam", "重新训练模型"))
        self.clearButton.setText(_translate("Spam", "清空"))

