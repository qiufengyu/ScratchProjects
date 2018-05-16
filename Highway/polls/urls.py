from django.urls import path

from . import views

urlpatterns = [
    path('', views.index, name='index'),
    path('about/', views.about, name='about'),
    path('help/', views.help, name='help'),
    path('incidents/', views.filtering, name='incidents'),
    path('incidents/filtering/', views.filtering, name='filtering'),
    path('incidents/detail/', views.detail, name='detail'),
    path('incidents/add/', views.add, name='add'),
    path('incidents/detail/<event>',views.detail_with_id, name='detail_with_id'),
    path('analysis/', views.AnalysisView.as_view(), name='analysis'),
]