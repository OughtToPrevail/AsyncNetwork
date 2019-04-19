/*
Copyright 2019 https://github.com/OughtToPrevail

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package oughttoprevail.asyncnetwork.packet;

import java.nio.ByteBuffer;

public interface PassedNumber<T extends Number>
{
	/**
	 * Returns a {@link Number} read from the specified byteBuffer.
	 *
	 * @param byteBuffer containing the {@link Number} to return
	 * @return a {@link Number} read from the specified byteBuffer
	 */
	T get(ByteBuffer byteBuffer);
	
	/**
	 * Returns the size in bytes of the number returned from {@link PassedNumber}.
	 *
	 * @return the size in bytes of the number returned from {@link PassedNumber}
	 */
	int getSize();
}