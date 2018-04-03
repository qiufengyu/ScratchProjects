from django.urls import path

from . import views

urlpatterns = [
    path('', views.IndexView.as_view(), name='index'),
    path('analysis', views.AnalysisView.as_view(), name='analysis'),
]