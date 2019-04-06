from django.urls import path

from . import views

urlpatterns = [
    path('', views.index, name='index'),
    path('about/', views.about, name="about"),
    path('search/', views.search, name="search"),
    path('news/<int:id>/', views.news, name="news"),
]