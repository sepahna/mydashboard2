from django.conf import settings
from django.db import models

class QuickLink(models.Model):
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="dashboard_links")
    title = models.CharField(max_length=160)
    url = models.URLField(max_length=2048)
    icon = models.CharField(max_length=80, default="link")
    category = models.CharField(max_length=100, default="عمومی")
    pinned = models.BooleanField(default=False)
    client_uuid = models.UUIDField(db_index=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["-pinned", "title"]
        constraints = [models.UniqueConstraint(fields=["owner", "client_uuid"], name="unique_link_client_uuid")]
