package com.moderocky.guardian.logic.handler;

import com.moderocky.guardian.logic.ascendancy.Polytope;
import com.moderocky.guardian.logic.shape.Vertex;

public enum TransType {

    SCALE {
        @Override
        public Double sanitiseInput(Number number) {
            return (double) number;
        }

        @Override
        public void accept(Polytope polytope, double[] origin, Number scale) {
            if (origin.length != 3)
                throw new IllegalArgumentException("The origin must be a tri-positional coordinate.");
            double value = sanitiseInput(scale);
            for (Vertex vertex : polytope.vertices()) {
                vertex.setX(((vertex.getX() - origin[0]) * value) + origin[0]);
                vertex.setY(((vertex.getY() - origin[1]) * value) + origin[1]);
                vertex.setZ(((vertex.getZ() - origin[2]) * value) + origin[2]);
            }
        }

    },

    MIRROR {
        @Override
        public Integer sanitiseInput(Number number) {
            return Math.min(Math.max((int) number, 0), 2);
        }

        @Override
        public void accept(Polytope polytope, double[] origin, Number axis) {
            if (origin.length != 3)
                throw new IllegalArgumentException("The origin must be a tri-positional coordinate.");
            int ax = sanitiseInput(axis);
            switch (ax) {
                case 0:
                    for (Vertex vertex : polytope.vertices()) vertex.setX(vertex.getX() * -1);
                case 1:
                    for (Vertex vertex : polytope.vertices()) vertex.setY(vertex.getY() * -1);
                case 2:
                    for (Vertex vertex : polytope.vertices()) vertex.setZ(vertex.getZ() * -1);
            }
        }

    },

    ROTATE_X {
        @Override
        public Double sanitiseInput(Number number) {
            return Math.toRadians((double) number);
        }

        @Override
        public void accept(Polytope polytope, double[] origin, Number rotation) {
            if (origin.length != 3)
                throw new IllegalArgumentException("The origin must be a tri-positional coordinate.");
            double rot = sanitiseInput(rotation);

            for (Vertex vertex : polytope.vertices()) {
                double newY, newZ;

                newY = origin[1] + Math.cos(rot) * (vertex.getY() - origin[1]) - Math.sin(rot) * (vertex.getZ() - origin[2]);
                newZ = origin[2] + Math.sin(rot) * (vertex.getY() - origin[1]) + Math.cos(rot) * (vertex.getZ() - origin[2]);
                vertex.setY(newY);
                vertex.setZ(newZ);
            }
        }

    },

    ROTATE_Y {
        @Override
        public Double sanitiseInput(Number number) {
            return Math.toRadians((double) number);
        }

        @Override
        public void accept(Polytope polytope, double[] origin, Number rotation) {
            if (origin.length != 3)
                throw new IllegalArgumentException("The origin must be a tri-positional coordinate.");
            double rot = sanitiseInput(rotation);

            for (Vertex vertex : polytope.vertices()) {
                double newX, newZ;

                newX = origin[0] + Math.cos(rot) * (vertex.getX() - origin[0]) - Math.sin(rot) * (vertex.getZ() - origin[2]);
                newZ = origin[1] + Math.sin(rot) * (vertex.getX() - origin[0]) + Math.cos(rot) * (vertex.getZ() - origin[2]);
                vertex.setX(newX);
                vertex.setZ(newZ);
            }
        }

    },

    ROTATE_Z {
        @Override
        public Double sanitiseInput(Number number) {
            return Math.toRadians((double) number);
        }

        @Override
        public void accept(Polytope polytope, double[] origin, Number rotation) {
            if (origin.length != 3)
                throw new IllegalArgumentException("The origin must be a tri-positional coordinate.");
            double rot = sanitiseInput(rotation);

            for (Vertex vertex : polytope.vertices()) {
                double newX, newY;

                newX = origin[0] + Math.cos(rot) * (vertex.getX() - origin[0]) - Math.sin(rot) * (vertex.getY() - origin[1]);
                newY = origin[1] + Math.sin(rot) * (vertex.getX() - origin[0]) + Math.cos(rot) * (vertex.getY() - origin[1]);
                vertex.setX(newX);
                vertex.setY(newY);
            }
        }

    };

    public abstract Number sanitiseInput(Number number);

    public abstract void accept(Polytope polytope, double[] origin, Number value);

}
