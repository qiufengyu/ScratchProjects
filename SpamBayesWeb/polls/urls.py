from django.urls import path

from . import views

urlpatterns = [
    path('', views.index, name='index'),
    path('add', views.add, name='add'),
    path('change', views.change, name='change'),
    path('analysis', views.analysis, name='analysis'),
    path('addone', views.addone, name='addone'),
    path('spam/data/<path>/<file>', views.view_wrong, name='one_email'),
]