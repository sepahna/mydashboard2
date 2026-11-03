from rest_framework.viewsets import ModelViewSet
from .models import Note
from .serializers import NoteSerializer

class NoteViewSet(ModelViewSet):
    serializer_class = NoteSerializer

    def get_queryset(self):
        return Note.objects.filter(owner=self.request.user)

    def perform_create(self, serializer):
        serializer.save(owner=self.request.user)
