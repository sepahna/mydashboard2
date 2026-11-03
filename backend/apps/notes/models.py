from django.conf import settings
from django.db import models

class Note(models.Model):
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="dashboard_notes")
    title = models.CharField(max_length=240)
    body = models.TextField(blank=True)
    pinned = models.BooleanField(default=False)
    locked = models.BooleanField(default=False)
    archived = models.BooleanField(default=False)
    client_uuid = models.UUIDField(db_index=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["-pinned", "-updated_at"]
        constraints = [models.UniqueConstraint(fields=["owner", "client_uuid"], name="unique_note_client_uuid")]
