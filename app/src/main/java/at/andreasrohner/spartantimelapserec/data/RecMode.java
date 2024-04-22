/*
 * Spartan Time Lapse Recorder - Minimalistic android time lapse recording app
 * Copyright (C) 2014  Andreas Rohner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.andreasrohner.spartantimelapserec.data;

/**
 * Recording Mode
 */
public enum RecMode {
	
	/**
	 * Recording a video
	 */
	VIDEO,

	/**
	 * Recording a timelapse and store it as video
	 */
	VIDEO_TIME_LAPSE,

	/**
	 * Recording timelapse as images
	 */
	IMAGE_TIME_LAPSE,

	/**
	 * Record timelapse with camera 2
	 */
	CAMERA2_TIME_LAPSE
}
