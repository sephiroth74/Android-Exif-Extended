/*
 * utils.h
 *
 *  Created on: Jul 7, 2013
 *      Author: alessandro
 */

#ifndef UTILS_H_
#define UTILS_H_

/**
 * Given one of the resolution unit used in the EXIF Tags ( ResolutionUnit and FocalPlaneResolutionUnit )
 * this methods will return the resolution value associated ( in millimeters )
 * @param resolutionUnit
 * @return
 */
double computeResolutionUnit( int resolutionUnit )
{
	double result = 0;
	switch( resolutionUnit) {
		  case 1:
			  result = 25.4;
			  break; // inch
		  case 2:
				// According to the information I was using, 2 means meters.
				// But looking at the Cannon powershot's files, inches is the only
				// sensible value.
				result = 25.4;
				break;

		  case 3:
			  result = 10;
			  break;  // centimeter
		  case 4:
			  result = 1;
			  break;  // millimeter
		  case 5:
			  result = .001;
			  break;  // micrometer
		  default:
			  result = 25.4;
			  break;
	 }
	return result;
}


#endif /* UTILS_H_ */
