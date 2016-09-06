/*
 * Copyright 2016 michael-simons.eu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * A web frontend and a small api to store an official Twitter archive.
 * The stuff you'll find here isn't nice. I'll use jOOR Reflect to access private
 * apis, brute force the JSON from the archive into the same format the official
 * Twitter api uses and so on.
 */
package ac.simons.tweetarchive.web;
