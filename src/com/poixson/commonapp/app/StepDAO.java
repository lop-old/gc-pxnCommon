package com.poixson.commonapp.app;

import java.lang.reflect.Method;

import com.poixson.commonapp.app.annotations.xAppStep;
import com.poixson.commonapp.app.annotations.xAppStep.StepType;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsString;


public class StepDAO {

	public final StepType type;
	public final int      step;
	public final String   name;
	public final String   title;
	public final Method   method;



	public StepDAO(final xAppStep annotation, final Method method) {
		if(annotation == null) throw new NullPointerException("annotation argument is required!");
		if(method     == null) throw new NullPointerException("method argument is required!");
		this.type = annotation.type();
		this.step = annotation.priority();
		// strip method down to name
		{
			String name = method.getName();
			name = utilsString.trims(name, "_");
			for(final String trim : new String[] {
					"startup",
					"start",
					"shutdown",
					"stop"
			}) {
				if(name.startsWith(trim))
					name = name.substring(trim.length());
				name = utilsString.trims(name, "_");
			}
			if(utils.isEmpty(name))
				name = utilsString.trims(method.getName(), "_");
			this.name = name;
		}
		this.title = utils.isEmpty(annotation.title()) ? this.name : annotation.title();
		this.method = method;
	}



}
