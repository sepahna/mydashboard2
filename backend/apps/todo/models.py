from django.conf import settings
from django.db import models

class Task(models.Model):
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="dashboard_tasks")
    list_id = models.PositiveSmallIntegerField(default=1)
    title = models.CharField(max_length=240)
    details = models.TextField(blank=True)
    priority = models.PositiveSmallIntegerField(default=1)
    due_at = models.DateTimeField(null=True, blank=True)
    completed = models.BooleanField(default=False)
    pinned = models.BooleanField(default=False)
    client_uuid = models.UUIDField(db_index=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["completed", "-pinned", "-created_at"]
        constraints = [models.UniqueConstraint(fields=["owner", "client_uuid"], name="unique_task_client_uuid")]
