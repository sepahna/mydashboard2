from rest_framework.viewsets import ModelViewSet
from .models import QuickLink
from .serializers import QuickLinkSerializer

class LinkViewSet(ModelViewSet):
    serializer_class = QuickLinkSerializer

    def get_queryset(self):
        return QuickLink.objects.filter(owner=self.request.user)

    def perform_create(self, serializer):
        serializer.save(owner=self.request.user)
