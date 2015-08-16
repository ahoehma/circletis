package com.mymita.circletis;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.LinkedList;
import java.util.List;

public class Circletis extends ApplicationAdapter {

    // rendering
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    // data
    private Board board;
    private int activeCircleIndex = 0;

    static Vector2 addV2(Vector2 a, Vector2 b) {
        Vector2 r = new Vector2();
        r.add(a).add(b);
        return r;
    }

    @Override
    public void create() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
        shapeRenderer = new ShapeRenderer();
        board = new Board(new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2), Gdx.graphics.getHeight() / 3.5f, 12, 24);
    }

    @Override
    public void render() {

        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        // update circle rotations ...
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            if (activeCircleIndex < board.circles.size() - 1) {
                activeCircleIndex++;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (activeCircleIndex > 0) {
                activeCircleIndex--;
            }
        }
        for (int ci = 0; ci < board.circles.size(); ci++) {
            Circle circle = board.circles.get(ci);
            float ddBoardRotation = 0;
            if (ci == activeCircleIndex) {
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                    ddBoardRotation = -2; // degrees/s^2
                }
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                    ddBoardRotation = +2; // degrees/s^2
                }
            }
            ddBoardRotation += -1.5f * circle.dBoardRotationP;
            circle.boardRotationOffset = (float) (0.5f * ddBoardRotation * Math.sqrt(Gdx.graphics.getDeltaTime()) +//
                    circle.dBoardRotationP * Gdx.graphics.getDeltaTime() +//
                    circle.boardRotationOffset);
            circle.dBoardRotationP = ddBoardRotation * Gdx.graphics.getDeltaTime() + circle.dBoardRotationP;

            // TODO normalize board-rotation-offset at segments ... smooth animation between segments but the result must be snapping to the next segment
            float degreesPerSegment = 360 / board.numberOfSegmentsPerCircle;
            for (CircleSegment sg : circle.segments) {
                float newAngle0 = sg.angle0 + circle.boardRotationOffset;
                float newAngle1 = sg.angle1 + circle.boardRotationOffset;
                sg.update(sg.radius0, sg.radius1, newAngle0, newAngle1);
            }
            System.out.println(circle.boardRotationOffset);
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (int ci = 0; ci < board.circles.size(); ci++) {
            Circle circle = board.circles.get(ci);
            Color color = Color.RED;
            if (ci != activeCircleIndex) {
                float rAvg = (circle.radius0 + circle.radius1) / 2;
                float rNormalized = rAvg * MathUtils.PI / board.radius;
                float rgb = MathUtils.sin(rNormalized);
                color = new Color(rgb, rgb, rgb, 1);
            }
            for (CircleSegment sg : circle.segments) {
                drawCircleSegment(shapeRenderer, sg, circle.origin, color);
            }
        }

        shapeRenderer.end();
    }

    private void drawCircleSegment(ShapeRenderer shapeRenderer, CircleSegment sg, Vector2 origin, Color color) {

        final Vector2 v1Abs = addV2(origin, sg.v1);
        final Vector2 v2Abs = addV2(origin, sg.v2);
        final Vector2 v3Abs = addV2(origin, sg.v3);
        final Vector2 v4Abs = addV2(origin, sg.v4);

        shapeRenderer.setColor(color);

        // just a polygon
        //shapeRenderer.polygon(vertices(v1Abs, v2Abs, v3Abs, v4Abs));

        // the left/right borders of a segment
        shapeRenderer.line(v1Abs, v2Abs);
        shapeRenderer.line(v3Abs, v4Abs);

        // just a straight line on top
        //shapeRenderer.line(v2Abs, v3Abs);

        // maybe bette
        // Vector2 v2AbsC = new Vector2(v2Abs);
        // v2AbsC.scl(1.1f,1.1f);
        // Vector2 v3AbsC = new Vector2(v3Abs);
        // v3AbsC.scl(0.9f,1.1f);
        // shapeRenderer.curve(v2Abs.x, v2Abs.y, v2AbsC.x, v2AbsC.y,  v3AbsC.x, v3AbsC.y, v3Abs.x, v3Abs.y,64);

        shapeRenderer.circle(origin.x, origin.y, sg.radius0, 256);
    }

    private float[] vertices(Vector2 v1Abs, Vector2 v2Abs, Vector2 v3Abs, Vector2 v4Abs) {
        return new float[]{v1Abs.x, v1Abs.y, v2Abs.x, v2Abs.y, v3Abs.x, v3Abs.y, v4Abs.x, v4Abs.y};
    }

    static class CircleSegment {
        Vector2 v1, v2, v3, v4;
        float radius0;
        float radius1;
        float angle0;
        float angle1;

        void update(float radius0, float radius1, float angle0, float angle1) {
            this.radius0 = radius0;
            this.radius1 = radius1;
            this.angle0 = angle0;
            this.angle1 = angle1;
            if (angle0 < 0) {
                angle0 = 360 - (-angle0);
            }
            if (angle0 > 360) {
                angle0 = angle0 - 360;
            }
            if (angle1 < 0) {
                angle1 = 360 - (-angle1);
            }
            if (angle1 > 360) {
                angle1 = angle1 - 360;
            }
            // v1..v4 are relative to 0,0
            v1 = new Vector2(radius0 * MathUtils.cosDeg(angle0), radius0 * MathUtils.sinDeg(angle0));
            v2 = new Vector2(radius1 * MathUtils.cosDeg(angle0), radius1 * MathUtils.sinDeg(angle0));
            v3 = new Vector2(radius1 * MathUtils.cosDeg(angle1), radius1 * MathUtils.sinDeg(angle1));
            v4 = new Vector2(radius0 * MathUtils.cosDeg(angle1), radius0 * MathUtils.sinDeg(angle1));
        }
    }

    static class Circle {
        Vector2 origin;
        float radius0; // start radius of circle
        float radius1; // end radius of circle
        float deep; // radius + deep
        LinkedList<CircleSegment> segments = new LinkedList<CircleSegment>();
        float dBoardRotationP;
        float boardRotationOffset;
    }

    static class Board {

        final List<Circle> circles = new LinkedList<Circle>();
        Vector2 origin;
        float radius;
        int numberOfCircles;
        private int numberOfSegmentsPerCircle;

        Board(Vector2 origin, float radius, int numberOfCircles, int numberOfSegmentsPerCircle) {
            this.origin = origin;
            this.radius = radius;
            this.numberOfCircles = numberOfCircles;
            this.numberOfSegmentsPerCircle = numberOfSegmentsPerCircle;
            init();
        }

        private void init() {
            float deepPerCircle = radius / numberOfCircles;
            float degreesPerSegment = 360 / this.numberOfSegmentsPerCircle;
            for (int cn = 0; cn < numberOfCircles; cn++) {
                final Circle circle = new Circle();
                circle.origin = this.origin;
                circle.radius0 = (cn * deepPerCircle);
                circle.radius1 = (cn * deepPerCircle) + deepPerCircle;
                circle.deep = deepPerCircle;
                for (int sn = 0; sn < this.numberOfSegmentsPerCircle; sn++) {
                    float angle0 = degreesPerSegment * sn;
                    float angle1 = angle0 + degreesPerSegment;
                    final CircleSegment circleSegment = new CircleSegment();
                    circleSegment.update(circle.radius0, circle.radius1, angle0, angle1);
                    circle.segments.add(circleSegment);
                }
                this.circles.add(circle);
            }
        }
    }
}
