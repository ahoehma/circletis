package com.mymita.circletis;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Circletis extends ApplicationAdapter {

	static class Board {

		final List<Circle> circles = new LinkedList<Circle>();
		Vector2 origin;
		float radius;
		int numberOfCircles;
		private final int numberOfSegmentsPerCircle;
		private final float rotationSpeed;

		Board(final Vector2 origin, final float radius,
				final int numberOfCircles, final int numberOfSegmentsPerCircle,
				final float rotationSpeed) {
			this.origin = origin;
			this.radius = radius;
			this.numberOfCircles = numberOfCircles;
			this.numberOfSegmentsPerCircle = numberOfSegmentsPerCircle;
			this.rotationSpeed = rotationSpeed;
			init();
		}

		private void init() {
			circles.clear();
			final float deepPerCircle = radius / numberOfCircles;
			final float degreesPerSegment = 360 / this.numberOfSegmentsPerCircle;
			for (int cn = 0; cn < numberOfCircles; cn++) {
				final Circle circle = new Circle();
				circle.origin = this.origin;
				circle.radius0 = (cn * deepPerCircle);
				circle.radius1 = (cn * deepPerCircle) + deepPerCircle;
				circle.deep = deepPerCircle;
				for (int sn = 0; sn < this.numberOfSegmentsPerCircle; sn++) {
					final float angle0 = degreesPerSegment * sn;
					final float angle1 = angle0 + degreesPerSegment;
					final CircleSegment circleSegment = new CircleSegment();
					circleSegment.update(circle.radius0, circle.radius1,
							angle0, angle1);
					circle.segments.add(circleSegment);
				}
				this.circles.add(circle);
			}
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

	static class CircleSegment {
		Vector2 v1, v2, v3, v4;
		float radius0;
		float radius1;
		float angle0;
		float angle1;

		@Override
		public String toString() {
			return String.format("angle0: %f, angle1: %f", angle0, angle1);
		}

		void update(final float radius0, final float radius1,
				final float angle0, final float angle1) {
			this.radius0 = radius0;
			this.radius1 = radius1;
			this.angle0 = angle0;
			this.angle1 = angle1;
			// v1..v4 are relative to 0,0
			v1 = new Vector2(this.radius0 * MathUtils.cosDeg(this.angle0),
					this.radius0 * MathUtils.sinDeg(this.angle0));
			v2 = new Vector2(this.radius1 * MathUtils.cosDeg(this.angle0),
					this.radius1 * MathUtils.sinDeg(this.angle0));
			v3 = new Vector2(this.radius1 * MathUtils.cosDeg(this.angle1),
					this.radius1 * MathUtils.sinDeg(this.angle1));
			v4 = new Vector2(this.radius0 * MathUtils.cosDeg(this.angle1),
					this.radius0 * MathUtils.sinDeg(this.angle1));
		}
	}

	static Vector2 addV2(final Vector2 a, final Vector2 b) {
		final Vector2 r = new Vector2();
		r.add(a).add(b);
		return r;
	}

	// rendering
	private OrthographicCamera camera;

	private ShapeRenderer shapeRenderer;

	// data
	private Board board;

	@Override
	public void create() {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		camera.position.set(Gdx.graphics.getWidth() / 2,
				Gdx.graphics.getHeight() / 2, 0);
		shapeRenderer = new ShapeRenderer();
		final int numberOfCircles = 12;
		final int numberOfSegmentsPerCircle = 24;
		final float boardRadius = Math.min(Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight()) / 2.5f;
		final Vector2 boardOrigin = new Vector2(Gdx.graphics.getWidth() / 2,
				boardRadius + 60);
		final float rotationSpeed = 5; // degree/sec
		board = new Board(boardOrigin, boardRadius, numberOfCircles,
				numberOfSegmentsPerCircle, rotationSpeed);
	}

	private void drawCircleSegment(final ShapeRenderer shapeRenderer,
			final CircleSegment sg, final Vector2 origin, final Color color) {

		final Vector2 v1Abs = addV2(origin, sg.v1);
		final Vector2 v2Abs = addV2(origin, sg.v2);
		final Vector2 v3Abs = addV2(origin, sg.v3);
		final Vector2 v4Abs = addV2(origin, sg.v4);

		shapeRenderer.setColor(color);

		shapeRenderer.line(v1Abs, v2Abs);
		shapeRenderer.line(v3Abs, v4Abs);
		shapeRenderer.circle(origin.x, origin.y, sg.radius0, 256);
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		rotate(board, board.rotationSpeed * Gdx.graphics.getDeltaTime());
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		for (int ci = 0; ci < board.circles.size(); ci++) {
			final Circle circle = board.circles.get(ci);
			final float rAvg = (circle.radius0 + circle.radius1) / 2;
			final float rNormalized = rAvg * MathUtils.PI / board.radius;
			final float rgb = MathUtils.sin(rNormalized);
			final Color color = new Color(rgb, rgb, rgb, 1);
			for (final CircleSegment sg : circle.segments) {
				drawCircleSegment(shapeRenderer, sg, circle.origin, color);
			}
		}
		shapeRenderer.end();
	}

	@Override
	public void resize(final int width, final int height) {
		super.resize(width, height);
		final float boardRadius = Math.min(width, height) / 2.5f;
		final Vector2 boardOrigin = new Vector2(width / 2, boardRadius + 60);
		board.origin = boardOrigin;
		board.radius = boardRadius;
		board.init();
	}

	private void rotate(final Board board, final float w) {
		for (int ci = 0; ci < board.circles.size(); ci++) {
			final Circle circle = board.circles.get(ci);
			for (final CircleSegment sg : circle.segments) {
				float newAngle0 = sg.angle0 + w;
				float newAngle1 = sg.angle1 + w;
				if (newAngle0 > 360) {
					newAngle0 = newAngle0 - 360;
				}
				if (newAngle1 > 360) {
					newAngle1 = newAngle1 - 360;
				}
				assert newAngle0 >= 0;
				assert newAngle0 <= 360;
				assert newAngle1 >= 0;
				assert newAngle1 <= 360;
				sg.update(sg.radius0, sg.radius1, newAngle0, newAngle1);
				System.out
				.println(String.format("{c:%d} w: %f, %s", ci, w, sg));
			}
		}
		System.out.println();
	}
}
