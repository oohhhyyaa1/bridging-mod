package dev.hybridbridging;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class PlacementLogic {
    private PlacementLogic() {}

    public static double scoreCandidate(Direction supportFace, Vec3d movementVector,
            boolean diagonalAssistance, boolean edgeAssistance, Vec3d hitPoint,
            Vec3d rayStart, Vec3d rayEnd, double tolerance) {
        double safeTolerance = Double.isFinite(tolerance) ? Math.max(0.01d, tolerance) : 0.25d;
        Vec3d horizontalMovement = new Vec3d(movementVector.x, 0.0d, movementVector.z);
        Vec3d faceDirection = new Vec3d(supportFace.getOffsetX(), 0.0d, supportFace.getOffsetZ());

        double alignment = 0.0d;
        if (horizontalMovement.lengthSquared() > 1.0E-6d && faceDirection.lengthSquared() > 0.0d) {
            alignment = faceDirection.dotProduct(horizontalMovement.normalize());
        }

        double score = alignment * 40.0d;
        if (diagonalAssistance && alignment > 0.5d) score += alignment * 8.0d;
        if (edgeAssistance && alignment > 0.5d) score += alignment * 12.0d;

        double rayDistance = distanceToSegment(hitPoint, rayStart, rayEnd);
        double rayProximity = Math.max(0.0d, 1.0d - rayDistance / safeTolerance);
        return score + rayProximity * 20.0d;
    }

    public static double distanceToSegment(Vec3d point, Vec3d segmentStart, Vec3d segmentEnd) {
        Vec3d segment = segmentEnd.subtract(segmentStart);
        double lengthSquared = segment.lengthSquared();
        if (lengthSquared < 1.0E-12d) return point.distanceTo(segmentStart);
        double projection = point.subtract(segmentStart).dotProduct(segment) / lengthSquared;
        double clampedProjection = Math.max(0.0d, Math.min(1.0d, projection));
        return point.distanceTo(segmentStart.add(segment.multiply(clampedProjection)));
    }
}
