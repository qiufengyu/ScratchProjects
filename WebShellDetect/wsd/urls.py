from django.urls import path

from . import views

urlpatterns = [
    path('', views.index, name='index'),
    path('analysis/', views.analysis, name='analysis'),
    path('about/', views.about, name='about'),
]