from rest_framework import serializers
from .models import QuickLink

class QuickLinkSerializer(serializers.ModelSerializer):
    class Meta:
        model = QuickLink
        fields = "__all__"
        read_only_fields = ["owner", "created_at", "updated_at"]
