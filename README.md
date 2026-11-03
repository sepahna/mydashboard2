# MyDashboard

داشبورد شخصی اندروید برای ساعت و تاریخ، یادداشت‌ها، دو فهرست مستقل To-Do و لینک‌های سریع.

## وضعیت پروژه

این Repository یک نسخه پایه قابل توسعه (MVP foundation) است. قابلیت‌های محلی اصلی شامل ایجاد/ویرایش/حذف یادداشت، دو فهرست وظایف مستقل، افزودن/حذف لینک و داشبورد خلاصه در کد پیاده‌سازی شده‌اند. قابلیت‌های پیشرفته‌ای مانند قفل رمزنگاری‌شده یادداشت، اعلان زمان‌بندی‌شده، همگام‌سازی دوطرفه، تقویم کامل شمسی و تاریخچه نسخه‌ها هنوز نیازمند توسعه و تست تکمیلی هستند؛ وجود ساختار Backend به معنی تکمیل این قابلیت‌ها نیست.

## فناوری‌ها

- Kotlin, Jetpack Compose, Material 3
- Room / SQLite
- WorkManager
- Retrofit / OkHttp
- Django REST Framework / PostgreSQL
- GitHub Actions

## پیش‌نیاز Android

- Android Studio نسخه جدید
- JDK 17
- Android SDK Platform 35
- Gradle wrapper

## اجرای اندروید

1. پروژه را در Android Studio باز کنید.
2. اگر Gradle wrapper وجود ندارد، از Android Studio گزینه Sync Project with Gradle Files را اجرا کنید یا در محیطی که Gradle نصب است اجرا کنید:

   ```bash
   gradle wrapper --gradle-version 8.11.1
   ```

3. سپس:

   ```bash
   ./gradlew :android:app:assembleDebug
   ```

4. خروجی:

   `android/app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions

Workflow ساخت APK در `.github/workflows/build-apk.yml` قرار دارد. پس از Push به شاخه `main` یا اجرای دستی از تب Actions، Artifact با نام `app-debug-apk` منتشر می‌شود.

Workflow در GitHub ابتدا Gradle 8.11.1 را نصب و فایل‌های Wrapper را تولید می‌کند؛ بنابراین برای اجرای CI نیازی به Commit کردن فایل‌های باینری Wrapper نیست. برای Build محلی می‌توانید دستور `gradle wrapper --gradle-version 8.11.1` را اجرا کنید.

## Backend

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements/base.txt
cp .env.example .env
```

PostgreSQL را راه‌اندازی کنید، مقادیر `.env` را تنظیم کنید، سپس:

```bash
python manage.py makemigrations notes todo links
python manage.py migrate
python manage.py createsuperuser
python manage.py runserver
```

API health: `GET /api/health/`

APIهای Notes، Tasks و Links نیازمند JWT هستند. مسیرهای صدور توکن JWT در نسخه پایه هنوز اضافه نشده‌اند و باید پیش از اتصال اپلیکیشن به سرور تکمیل شوند.

## امنیت

- مقادیر محرمانه را در `.env` یا GitHub Secrets نگه دارید.
- مقدار `DJANGO_SECRET_KEY` را در محیط عملیاتی حتماً عوض کنید.
- از DEBUG در محیط عملیاتی استفاده نکنید.
- انتشار Release نیازمند تنظیم signing امن است؛ Workflow فعلی AAB بدون امضای انتشار سفارشی می‌سازد.

## مجوز

برای استفاده شخصی و توسعه داخلی. پیش از انتشار تجاری، مجوزها و سیاست حریم خصوصی را تکمیل کنید.
