package com.poixson.utils.byref;


public interface StringRefInterface {



	public void value(final String val);
	public String value();

	public boolean isEmpty();
	public boolean notEmpty();

	public int length();



	// ------------------------------------------------------------------------------- //
	// index of



	// index of (single delim)
	public int indexOf(final char delim);
	public int indexOf(final String delim);

	// index of (many delims)
	public int indexOf(final char...delims);
	public int indexOf(final String...delims);

	// last index of (single delim)
	public int indexOfLast(final char delim);
	public int indexOfLast(final String delim);

	// last index of (many delims)
	public int indexOfLast(final char...delims);
	public int indexOfLast(final String...delims);



	// ------------------------------------------------------------------------------- //
	// get/cut first part



	// get first part (single delim)
	public String peekFirstPart(final char   delim);
	public String peekFirstPart(final String delim);
	// cut first part (single delim)
	public String cutFirstPart(final char    delim);
	public String cutFirstPart(final String  delim);

	// get first part (many delims)
	public String peekFirstPart(final char  ...delims);
	public String peekFirstPart(final String...delims);
	// cut first part (many delims)
	public String cutFirstPart(final char   ...delims);
	public String cutFirstPart(final String ...delims);



	// ------------------------------------------------------------------------------- //
	// get/cut last part



	// get last part (single delim)
	public String peekLastPart(final char   delim);
	public String peekLastPart(final String delim);
	// cut last part (single delim)
	public String cutLastPart(final char    delim);
	public String cutLastPart(final String  delim);

	// get last part (many delims)
	public String peekLastPart(final char  ...delims);
	public String peekLastPart(final String...delims);
	// cut last part (many delims)
	public String cutLastPart(final char   ...delims);
	public String cutLastPart(final String ...delims);



}
