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

import oughttoprevail.asyncnetwork.packet.read.ReadResult;
import oughttoprevail.asyncnetwork.packet.write.WritablePacketBuilder;

/**
 * A serializer and deserializer that puts an {@link Object} {@link T} into a {@link WritablePacketBuilder} and
 * reverses the {@link ReadResult} into the same {@link Object} {@link T}.
 * Note: it is recommended to make an inner private class of this {@link SerDes} instead of an
 * outer class so you can easily access the private fields.
 *
 * @param <T> object which will be serialized and deserialized
 */
public interface SerDes<T> extends Serializer<T>, Deserializer<T>
{
}