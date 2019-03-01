from django.urls import path

from . import views

urlpatterns = [
    path('', views.index, name='index'),
    path('register/', views.register, name='register'),
    path('login/', views.login, name='login'),
    path('logout/', views.logout, name='logout'),
    path('sendmail/', views.sendmail, name='sendmail'),
    path('mail_id/', views.mail, name='mail_id'),
    path('mail_id/<mid>', views.mail_id, name='mail_id_param'),
    path('outbox/', views.outbox, name='outbox'),
    path('spambox/', views.spambox, name='spambox'),
    path('deletebox/', views.deletebox, name='deletebox'),
    path('markspam/', views.markspam, name='markspam'),
    path('markdelete/', views.markdelete, name='markdelete'),
    path('markdeletesend/', views.markdeletesend, name='markdeletesend'),
    path('markdeletesendorreceive/', views.markdeletesendorreceive, name='markdeletesendorreceive'),
    path('markread/', views.markread, name='markread'),
    path('restorespam/', views.restorespam, name='restorespam'),
    path('restoredelete/', views.restoredelete, name='restoredelete'),
    path('deleteforever/', views.deleteforever, name='deleteforever'),
    path('config/', views.config, name='config'),
    path('configadd/', views.configadd, name='configadd'),
    path('configdelete/', views.configdelete, name='configdelete'),
]