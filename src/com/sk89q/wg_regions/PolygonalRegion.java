// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.wg_regions;

import java.util.List;
import java.util.ArrayList;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import org.bukkit.Location;

public class PolygonalRegion extends Region {

    List<BlockVector2D> points;
    int minY;
    int maxY;
    private BlockVector min;
    private BlockVector max;

    public PolygonalRegion(String id, List<BlockVector2D> points, int minY, int maxY) {
        super(id);
        this.points = points;
        this.minY = minY;
        this.maxY = maxY;

        int minX = points.get(0).getBlockX();
        int minZ = points.get(0).getBlockZ();
        int maxX = points.get(0).getBlockX();
        int maxZ = points.get(0).getBlockZ();

        for (final BlockVector2D v : points) {
            int x = v.getBlockX();
            int z = v.getBlockZ();
            if (x < minX) {
                minX = x;
            }
            if (z < minZ) {
                minZ = z;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (z > maxZ) {
                maxZ = z;
            }
        }

        min = new BlockVector(minX, minY, minZ);
        max = new BlockVector(maxX, maxY, maxZ);
    }

    public List<BlockVector2D> getPoints() {
        return points;
    }

    @Override
    public BlockVector getMinimumPoint() {
        return min;
    }

    @Override
    public BlockVector getMaximumPoint() {
        return max;
    }

    /**
     * Checks to see if a point is inside this region.
     */
    @Override
    public boolean contains(Vector pt) {
        return contains(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()); 
    }
    
    
    @Override
    public boolean contains(Location loc) {
        return contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    
    private boolean contains(int targetX, int targetY, int targetZ){
        
        if (targetY < minY || targetY > maxY) {
            return false;
        }
        //Quick and dirty check.
        if (targetX < min.getBlockX() || targetX > max.getBlockX() || targetZ < min.getBlockZ() || targetZ > max.getBlockZ()) {
            return false;
        }
        boolean inside = false;
        final int npoints = points.size();
        int xNew, zNew;
        int xOld, zOld;
        int x1, z1;
        int x2, z2;
        int i;

        xOld = points.get(npoints - 1).getBlockX();
        zOld = points.get(npoints - 1).getBlockZ();

        for (i = 0; i < npoints; i++) {
            xNew = points.get(i).getBlockX();
            zNew = points.get(i).getBlockZ();
            //Check for corner
            if (xNew == targetX && zNew == targetZ) {
                return true;
            }
            if (xNew > xOld) {
                x1 = xOld;
                x2 = xNew;
                z1 = zOld;
                z2 = zNew;
            } else {
                x1 = xNew;
                x2 = xOld;
                z1 = zNew;
                z2 = zOld;
            }
            if ((xNew < targetX) == (targetX <= xOld)
                    && ((long) targetZ - (long) z1) * (long) (x2 - x1) <= ((long) z2 - (long) z1)
                    * (long) (targetX - x1)) {
                inside = !inside;
            }
            xOld = xNew;
            zOld = zNew;
        }

        return inside;
    }

    @Override
    public List<Region> getIntersectingRegions(List<Region> regions){// throws UnsupportedIntersectionException {
        final int numRegions = regions.size();
        final int numPoints = points.size();
        final List<Region> intersectingRegions = new ArrayList<Region>();
        int i, i2, i3;

        for (i = 0; i < numRegions; i++) {
            Region region = regions.get(i);
            final BlockVector rMinPoint = region.getMinimumPoint();
            final BlockVector rMaxPoint = region.getMaximumPoint();

            // Check whether the region is outside the min and max vector
            if ((rMinPoint.getBlockX() < min.getBlockX() && rMaxPoint.getBlockX() < min.getBlockX()) 
                            || (rMinPoint.getBlockX() > max.getBlockX() && rMaxPoint.getBlockX() > max.getBlockX())
                    && ((rMinPoint.getBlockY() < min.getBlockY() && rMaxPoint.getBlockY() < min.getBlockY())
                            || (rMinPoint.getBlockY() > max.getBlockY() && rMaxPoint.getBlockY() > max.getBlockY()))
                    && ((rMinPoint.getBlockZ() < min.getBlockZ() && rMaxPoint.getBlockZ() < min.getBlockZ())
                            || (rMinPoint.getBlockZ() > max.getBlockZ() && rMaxPoint.getBlockZ() > max.getBlockZ())) ) {
                intersectingRegions.add(regions.get(i));
                continue;
            }

            // Check whether the regions points are inside the other region
            for (i2 = 0; i < numPoints; i++) {
                final Vector pt = new Vector(points.get(i2).getBlockX(), minY, points.get(i2).getBlockZ());
                final Vector pt2 = new Vector(points.get(i2).getBlockX(), maxY, points.get(i2).getBlockZ());
                if (region.contains(pt) || region.contains(pt2)) {
                    intersectingRegions.add(regions.get(i));
                    continue;
                }
            }

            // Check whether the other regions points are inside the current region
            if (region instanceof PolygonalRegion) {
                for (i2 = 0; i < ((PolygonalRegion)region).getPoints().size(); i++) {
                    final BlockVector2D pt2Dr = ((PolygonalRegion)region).getPoints().get(i2);
                    final int minYr = ((PolygonalRegion)region).minY;
                    final int maxYr = ((PolygonalRegion)region).maxY;
                    final Vector ptr = new Vector(pt2Dr.getBlockX(), minYr, pt2Dr.getBlockZ());
                    final Vector ptr2 = new Vector(pt2Dr.getBlockX(), maxYr, pt2Dr.getBlockZ());

                    if (this.contains(ptr) || this.contains(ptr2)) {
                        intersectingRegions.add(regions.get(i));
                        continue;
                    }
                }
            } else if (region instanceof CuboidRegion) {
                final BlockVector ptcMin = region.getMinimumPoint(); 
                final BlockVector ptcMax = region.getMaximumPoint();

                if (this.contains(new Vector(ptcMin.getBlockX(), ptcMin.getBlockY(), ptcMin.getBlockZ()))
                        || this.contains(new Vector(ptcMin.getBlockX(), ptcMin.getBlockY(), ptcMax.getBlockZ()))
                        || this.contains(new Vector(ptcMin.getBlockX(), ptcMax.getBlockY(), ptcMax.getBlockZ()))
                        || this.contains(new Vector(ptcMin.getBlockX(), ptcMax.getBlockY(), ptcMin.getBlockZ()))
                        || this.contains(new Vector(ptcMax.getBlockX(), ptcMax.getBlockY(), ptcMax.getBlockZ()))
                        || this.contains(new Vector(ptcMax.getBlockX(), ptcMax.getBlockY(), ptcMin.getBlockZ()))
                        || this.contains(new Vector(ptcMax.getBlockX(), ptcMin.getBlockY(), ptcMin.getBlockZ()))
                        || this.contains(new Vector(ptcMax.getBlockX(), ptcMin.getBlockY(), ptcMax.getBlockZ())) ) {
                    intersectingRegions.add(regions.get(i));
                    continue;
                }
            } else {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            // Check whether the current regions edges collide with the regions edges
            boolean regionIsIntersecting = false;
            for (i2 = 0; i2 < numPoints; i2++) {
                boolean checkNextPoint = false;
                final BlockVector2D currPoint = points.get(i2);
                BlockVector2D nextPoint;

                if (i2 == (numPoints - 1)) {
                    nextPoint = points.get(0);
                } else {
                    nextPoint = points.get(i2 + 1);
                }

                int currX = currPoint.getBlockX();
                int currZ = currPoint.getBlockZ();

                while (!checkNextPoint) {
                    for(i3 = this.minY; i3 <= this.maxY; i3++) {
                        if (region.contains(new Vector(currX, i3, currZ))) {
                            intersectingRegions.add(regions.get(i));
                            regionIsIntersecting = true;
                            break;
                        }
                    }

                    if (currX == nextPoint.getBlockX() || currZ == nextPoint.getBlockZ() || regionIsIntersecting) {
                        checkNextPoint = true;
                    }

                    if (nextPoint.getBlockX() > currPoint.getBlockX()) {
                        currX++;
                    } else {
                        currX--;
                    }
                    if (nextPoint.getBlockZ() > currPoint.getBlockZ()) {
                        currZ++;
                    } else {
                        currZ--;
                    }
                }

                if (regionIsIntersecting) {
                    break;
                }
            }
        }

        return intersectingRegions;
    }


    /**
     * Return the type of region as a user-friendly name.
     * 
     * @return type of region
     */
    @Override
    public String getTypeName() {
        return "polygon";
    }

    /**
     * Get the number of Blocks in this region
     * 
     * @return
     */
    @Override
    public int volume() {
        int volume = 0;
        int numPoints = points.size();
        if (numPoints < 3) {
            return 0;
        }

        double area = 0;
        int xa, z1, z2;

        for (int i = 0; i <= numPoints - 1; i++) {
            xa = points.get(i).getBlockX();
            //za = points.get(i).getBlockZ();

            if (points.get(i + 1) == null) {
                z1 = points.get(0).getBlockZ();
            } else {
                z1 = points.get(i + 1).getBlockZ();
            }
            if (points.get(i - 1) == null) {
                z2 = points.get(numPoints - 1).getBlockZ();
            } else {
                z2 = points.get(i - 1).getBlockZ();
            }

            area = area + (xa * (z1 - z2));
        }

        xa = points.get(0).getBlockX();
        //za = points.get(0).getBlockZ();

        area = area + (xa * (points.get(1).getBlockZ() - points.get(numPoints - 1).getBlockZ()));

        volume = (Math.abs(maxY - minY) + 1) * (int) Math.ceil((Math.abs(area) / 2));

        return volume;
    }

}
