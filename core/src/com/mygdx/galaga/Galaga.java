package com.mygdx.galaga;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Galaga extends ApplicationAdapter 
{
	private final float RIGHTMARGIN = 116;
	private final float LEFTMARGIN = 100;
	private SpriteBatch batch;
	private Texture ship, enemy,Star,mm,em;
	private BitmapFont font;
	private Rectangle sh; 
	private OrthographicCamera camera;
	private Array<Rectangle> myMissiles, stars,enMissiles;
	private Array<Array<Rectangle>> enemies;
	private long starTime,lastShot,shotDelay,lastEnShot;
	private int level,direction,score;
	
	@Override
	public void create() 
	{
		level = 1;
		direction = 1;
		shotDelay = 5000; 
		lastEnShot = 0;
		score = 0;
		
		ship = new Texture(Gdx.files.internal("ship.jpg"));
		sh = new Rectangle();
		sh.x = 1366/2;
		sh.y = 20;
		sh.width = 30;
		sh.height = 36;
		
		myMissiles = new Array<Rectangle>();
		mm = new Texture(Gdx.files.internal("mymissile.jpg"));
		
		enMissiles = new Array<Rectangle>();
		em = new Texture(Gdx.files.internal("enemymissile.jpg"));
		
		enemy = new Texture(Gdx.files.internal("enemy.jpg"));
		enemies = new Array<Array<Rectangle>>();
		enemies.add(new Array<Rectangle>());
		spawnEnemies();
		
		Star = new Texture(Gdx.files.internal("Star.jpg"));
		stars = new Array<Rectangle>();
		for(int i=0; i < 200; i++)
		{
			Rectangle star = new Rectangle();
			star.width = 1;
			star.height = 1;
			stars.add(star);
		}
		positionStars();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false,1366,768);
		
		font = new BitmapFont();
		font.setColor(Color.RED);
		batch = new SpriteBatch();
		
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		
		
		batch.setProjectionMatrix(camera.combined);	
		batch.begin();
		for(Rectangle star: stars)
			batch.draw(Star,star.x,star.y);
		for(Array<Rectangle> arr: enemies)
			for(Rectangle en: arr)
				batch.draw(enemy,en.x,en.y);
		for(Rectangle m: myMissiles)
			batch.draw(mm,m.x,m.y);
		for(Rectangle r: enMissiles)
			batch.draw(em,r.x,r.y);
		font.draw(batch,"Score:" + score, 10, 100);
		font.draw(batch,"Level: " + level, 10, 120);
		batch.draw(ship,sh.x,sh.y);		
		batch.end();
		
		
		Iterator<Rectangle> iterMi = myMissiles.iterator();
		while(iterMi.hasNext())//handles movement of missiles
		{
			
			Rectangle curr = iterMi.next();
			
			if(curr.y > 750)
				iterMi.remove();
			else if(hit(curr))//remove missile from array if missile makes contact with an enemy
				iterMi.remove();
			else//moves missile upwards
				curr.y += 300 * Gdx.graphics.getDeltaTime();
		}
		
		Iterator<Rectangle> iterEnMi = enMissiles.iterator();
		while(iterEnMi.hasNext())
		{
			Rectangle mis = iterEnMi.next();
			
			if(mis.y < 0)
				iterEnMi.remove();
			else if(mis.overlaps(sh))
				iterEnMi.remove();
			else
				mis.y -= 300 * Gdx.graphics.getDeltaTime();
		}
		float min = 50,max = 0;
		for(Array<Rectangle> row: enemies)
		{
			if(row.size > 0)
			{
				if(row.get(row.size-1).x > max)
					max = row.get(row.size-1).x;
				if(row.get(0).x < min)
					min = row.get(0).x;
			}
		}
		if(noEnemies())//advances to next level IF array of enemies is empty
		{
			levelUp();
		}
		else if(direction == 1 && max >= 1260)//changes direction when
			direction = -1;			//enemies hit edge
		else if(direction == -1 && min <= 100)
			direction = 1;
		
		
		for(Array<Rectangle> row: enemies)
		{
			Iterator<Rectangle> iterEn = row.iterator();
			while(iterEn.hasNext())//moves enemies
			{
				Rectangle en = iterEn.next();
				en.x += direction * 20 * Gdx.graphics.getDeltaTime();			
			}
		}
				
		if(TimeUtils.nanoTime() - starTime > 125000000)//change position of stars after a delay
			positionStars();
		
		if(TimeUtils.nanosToMillis(TimeUtils.nanoTime()) - lastEnShot > shotDelay && enemies.size > 0)//random
		{																						     //enemy launches 
			Rectangle en = enemies.random().random();//missile after delay
			if(en != null)
			{
				enMissiles.add(new Rectangle(en.x + 15,en.y,8,24));
				lastEnShot = TimeUtils.nanosToMillis(TimeUtils.nanoTime());
			}
		}
		if(Gdx.input.isKeyPressed(Keys.SPACE) && TimeUtils.nanoTime() - lastShot > 125000000)//launch missile
		{																					//when space pressed
			spawnMyMissile();
			lastShot = TimeUtils.nanoTime();
		}
	/*	if(Gdx.input.isTouched())
		{
			Vector3 touchpos = new Vector3();
			touchpos.set(Gdx.input.getX(),Gdx.input.getY(),0);
			camera.unproject(touchpos);
			sh.x = touchpos.x - (64/2);
		}*/
		if(Gdx.input.isKeyPressed(Keys.LEFT))
			sh.x -= 200 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Keys.RIGHT))
			sh.x += 200 * Gdx.graphics.getDeltaTime();
		if(sh.x > 1366 - RIGHTMARGIN)//Keeps player from going too far right
			sh.x = 1366-RIGHTMARGIN;
		if(sh.x < LEFTMARGIN)//Keeps player from going too far left
			sh.x = LEFTMARGIN;		
	}
	
	public void positionStars()
	{
		for(Rectangle star: stars)
		{ 
			star.x = MathUtils.random(0,1366);
			star.y = MathUtils.random(0, 768);
		}
		starTime = TimeUtils.nanoTime();
	}
	
	public void spawnEnemies()
	{
		int w = 80, h = 700;
		for(Array<Rectangle> row: enemies)
		{
			for(int i = 0; i < 10; i++)
        	{  
				Rectangle en = new Rectangle();
				en.x = w;
				en.y = h;
				en.width = 40;
				en.height = 32;
				row.add(en);
				w += 125; 
        	}
			h -= 30;
			w = 80;
		}
	}
	
	public void spawnMyMissile()
	{
		Rectangle mis = new Rectangle();
		mis.x = sh.x + 13;
		mis.y = 40;
		mis.height = 24;
		mis.width = 8;
		myMissiles.add(mis);
	}
	
	public boolean hit(Rectangle rec)
	{
		for(Array<Rectangle> row : enemies)
		{
			Iterator<Rectangle> Iter = row.iterator();
			while(Iter.hasNext())
			{
				Rectangle en = Iter.next();
				if(rec.overlaps(en))
				{
					score += 100;
					Iter.remove();
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean noEnemies()
	{
		for(Array<Rectangle> arr: enemies)
		{
			if(arr.size > 0)
				return false;
		}
		return true;
	}
	
	public void levelUp()
	{
		level++;
		if(level % 5 == 0)
			enemies.add(new Array<Rectangle>());
		else
			shotDelay -= 50;
		spawnEnemies();
	}
}