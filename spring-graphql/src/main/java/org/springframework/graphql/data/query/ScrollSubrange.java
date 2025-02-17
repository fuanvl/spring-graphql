/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.graphql.data.query;


import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.graphql.data.pagination.Subrange;
import org.springframework.lang.Nullable;

/**
 * Container for parameters that limit result elements to a subrange including a
 * relative {@link ScrollPosition}, number of elements, and direction.
 *
 * <p> For backward pagination, the offset of an {@link OffsetScrollPosition}
 * is adjusted to point to the first item in the range by subtracting the count
 * from it. Hence, for {@code OffsetScrollPosition} {@link #forward()} is
 * always {@code true}.
 *
 * @author Rossen Stoyanchev
 * @author Oliver Drotbohm
 * @since 1.2.0
 */
public final class ScrollSubrange extends Subrange<ScrollPosition> {


	@SuppressWarnings("unused")
	private ScrollSubrange(
			@Nullable ScrollPosition pos, @Nullable Integer count, boolean forward,
			@Nullable Object unused /* temporarily to differentiate from deprecated constructor */) {

		super(pos, count, forward);
	}

	/**
	 * Public constructor.
	 * @deprecated in favor of {@link #create}, to be removed in 1.3.
	 */
	@Deprecated(since = "1.2.4", forRemoval = true)
	public ScrollSubrange(@Nullable ScrollPosition pos, @Nullable Integer count, boolean forward) {
		super(initPosition(pos, count, forward), count, (pos instanceof OffsetScrollPosition || forward));
	}

	@Nullable
	private static ScrollPosition initPosition(@Nullable ScrollPosition pos, @Nullable Integer count, boolean forward) {
		if (!forward) {
			if (pos instanceof OffsetScrollPosition offsetPosition && count != null) {
				return offsetPosition.advanceBy(-count);
			}
			else if (pos instanceof KeysetScrollPosition keysetPosition) {
				pos = keysetPosition.backward();
			}
		}
		return pos;
	}


	/**
	 * Create a {@link ScrollSubrange} instance.
	 * @param position the position relative to which to scroll, or {@code null}
	 * for scrolling from the beginning
	 * @param count the number of elements requested
	 * @param forward whether to return elements after (true) or before (false)
	 * the element at the given position
	 * @return the created subrange
	 * @since 1.2.4
	 */
	public static ScrollSubrange create(@Nullable ScrollPosition position, @Nullable Integer count, boolean forward) {
		if (position instanceof OffsetScrollPosition offsetScrollPosition) {
			return initFromOffsetPosition(offsetScrollPosition, count, forward);
		}
		else if (position instanceof KeysetScrollPosition keysetScrollPosition) {
			return initFromKeysetPosition(keysetScrollPosition, count, forward);
		}
		else {
			return new ScrollSubrange(position, count, forward, null);
		}
	}

	private static ScrollSubrange initFromOffsetPosition(
			OffsetScrollPosition position, @Nullable Integer count, boolean forward) {

		if (!forward) {
			if (count != null) {
				if (position.getOffset() == 0) {
					count = 0;
				}
				else if (count >= position.getOffset()) {
					count = (int) (position.getOffset() - 1);
				}
				position = position.advanceBy(-count - 1);
			}
			forward = true;
		}
		return new ScrollSubrange(position, count, forward, null);
	}

	private static ScrollSubrange initFromKeysetPosition(
			KeysetScrollPosition position, @Nullable Integer count, boolean forward) {

		if (!forward) {
			position = position.backward();
		}
		return new ScrollSubrange(position, count, forward, null);
	}

}
