package com.pingsdorf.server.ping;

import java.util.List;

import com.pingsdorf.server.room.Room;

/** Geometry helpers for assigning pings to rooms by polygon containment. */
final class RoomGeometry {

    private RoomGeometry() {}

    /**
     * @return the id of the first room whose polygon contains (x,y), or {@code null}.
     *         Uses standard ray-casting point-in-polygon.
     */
    static String findRoomContaining(List<Room> rooms, double x, double y) {
        for (var r : rooms) {
            var points = r.getPoints();
            if (points == null || points.size() < 3) continue;
            if (containsPoint(points, x, y)) return r.getId();
        }
        return null;
    }

    private static boolean containsPoint(List<Room.Point> poly, double x, double y) {
        boolean inside = false;
        int n = poly.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = poly.get(i).x(), yi = poly.get(i).y();
            double xj = poly.get(j).x(), yj = poly.get(j).y();
            boolean intersect = ((yi > y) != (yj > y))
                    && (x < (xj - xi) * (y - yi) / (yj - yi + 1e-12) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }
}
