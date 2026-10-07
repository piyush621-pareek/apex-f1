APEX F1 — Frontend
==================

Pages:
  index.html      redirects to dashboard
  dashboard.html  championship standings (drivers + constructors, with images)
  pitwall.html    lap times, telemetry, weather, track map
  fsp.html        Fan Season Predictions (create/edit/delete + accuracy)
  manage.html     CRUD admin for Drivers, Teams, Races
  css/style.css   all styling (custom, no framework)
  js/api.js       API client (fetch helpers) + shared helpers
  js/main.js      navbar / active link
  img/            driver headshots, team logos, track maps

IMPORTANT
---------
These pages call the backend REST API using RELATIVE URLs (e.g. /api/drivers),
so they are meant to be SERVED BY the Spring Boot backend (the whole static/
folder lives inside the backend project at:
  src/main/resources/static/).

To see them working, run the backend and open http://localhost:8080
Opening the .html files directly with file:// will show the layout but the
data will not load (the /api calls need the running backend).

If you edit these files, keep the API paths relative (/api/...).
