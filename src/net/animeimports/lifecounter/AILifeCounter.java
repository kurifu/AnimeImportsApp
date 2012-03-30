package net.animeimports.lifecounter;

import net.animeimports.android.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AILifeCounter extends Activity {
	TextView life1 = null;
	TextView life2 = null;
	TextView poison1 = null;
	TextView poison2 = null;
	TextView incrLife1 = null;
	TextView incrLife2 = null;
	TextView decrLife1 = null;
	TextView decrLife2 = null;
	TextView incrPoison1 = null;
	TextView incrPoison2 = null;
	TextView decrPoison1 = null;
	TextView decrPoison2 = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		System.out.println("LifeCounter started!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lifecounter);
        life1 = (TextView) findViewById(R.id.tvLife1);
        life2 = (TextView) findViewById(R.id.tvLife2);
        poison1 = (TextView) findViewById(R.id.tvPoison1);
        poison2 = (TextView) findViewById(R.id.tvPoison2);
        incrLife1 = (TextView) findViewById(R.id.tvLifeIncr1);
        incrLife2 = (TextView) findViewById(R.id.tvLifeIncr2);
        decrLife1 = (TextView) findViewById(R.id.tvLifeDecr1);
        decrLife2 = (TextView) findViewById(R.id.tvLifeDecr2);
        incrPoison1 = (TextView) findViewById(R.id.tvPoisonIncr1);
        incrPoison2 = (TextView) findViewById(R.id.tvPoisonIncr2);
        decrPoison1 = (TextView) findViewById(R.id.tvPoisonDecr1);
        decrPoison2 = (TextView) findViewById(R.id.tvPoisonDecr2);
    }
	
	public void incrementLife(View v) {
		System.out.println("you clicked increment life!");
		if(v.getId() == incrLife1.getId()) {
			System.out.println("you clicked on life1!");
			int life = Integer.parseInt(life1.getText().toString());
			life1.setText(++life + "");
		}
		else if (v.getId() == incrLife2.getId()) {
			System.out.println("you clicked on life2!");
			int life = Integer.parseInt(life2.getText().toString());
			life2.setText(++life + "");
		}
	}
	
	public void decrementLife(View v) {
		System.out.println("you clicked decrement life!");
		if(v.getId() == decrLife1.getId()) {
			System.out.println("you clicked on life1!");
			int life = Integer.parseInt(life1.getText().toString());
			life1.setText(--life + "");
		}
		else if (v.getId() == decrLife2.getId()) {
			System.out.println("you clicked on life2!");
			int life = Integer.parseInt(life2.getText().toString());
			life2.setText(--life + "");
		}
	}
	
	public void incrementPoison(View v) {
		System.out.println("increment poinson!");
		if(v.getId() == incrPoison1.getId()) {
			int poison = Integer.parseInt(poison1.getText().toString());
			poison1.setText(++poison + "");
		}
		else if(v.getId() == incrPoison2.getId()) {
			int poison = Integer.parseInt(poison2.getText().toString());
			poison2.setText(++poison + "");
		}
	}
	
	public void decrementPoison(View v) {
		System.out.println("decrement poinson!");
		if(v.getId() == decrPoison1.getId()) {
			int poison = Integer.parseInt(poison1.getText().toString());
			poison1.setText(--poison + "");
		}
		else if(v.getId() == decrPoison2.getId()) {
			int poison = Integer.parseInt(poison2.getText().toString());
			poison2.setText(--poison + "");
		}
	}
	
	public void goBack(View v) {
		finish();
	}
}