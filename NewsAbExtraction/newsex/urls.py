from django.urls import path

from . import views

urlpatterns = [
    path('', views.IndexView.as_view(), name='index'),
    path('news/<int:id>/', views.NewsView.as_view(), name='news'),
]