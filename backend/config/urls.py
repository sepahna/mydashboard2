from django.contrib import admin
from django.urls import include, path
from rest_framework.routers import DefaultRouter
from apps.notes.views import NoteViewSet
from apps.todo.views import TaskViewSet
from apps.links.views import LinkViewSet
from apps.core.views import HealthView

router = DefaultRouter()
router.register("notes", NoteViewSet, basename="note")
router.register("tasks", TaskViewSet, basename="task")
router.register("links", LinkViewSet, basename="link")

urlpatterns = [
    path("admin/", admin.site.urls),
    path("api/health/", HealthView.as_view(), name="health"),
    path("api/", include(router.urls)),
]
