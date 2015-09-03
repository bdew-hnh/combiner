/*
 * This file is part of bdew's H&H map stitcher.
 * Copyright (C) 2015 bdew
 *
 * Redistribution and/or modification of this file is subject to the
 * terms of the GNU Lesser General Public License, version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Other parts of this source tree adhere to other copying
 * rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 * A copy the GNU Lesser General Public License is distributed along
 * with the source tree of which this file is a part in the file
 * `doc/LPGL-3'. If it is missing for any reason, please see the Free
 * Software Foundation's website at <http://www.fsf.org/>, or write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */

package net.bdew.hafen.combiner

object BadHashes {
  // Set of known bad hashes, those were generated in older versions when a map tile contains
  // only one kind of terrain. Tiles like that often happen in lakes, mountains and caves
  // and cause stitching errors.

  val bad = Set(
    "728bedc1251335e5",
    "db52df3d1d20c6e5",
    "4419d0b9152e57e5",
    "ace0c2350d3be8e5",
    "15a7b3b1054979e5",
    "7e6ea52cfd570ae5",
    "e73596a8f5649be5",
    "4ffc8824ed722ce5",
    "b8c379a0e57fbde5",
    "218a6b1cdd8d4ee5",
    "8a515c98d59adfe5",
    "f3184e14cda870e5",
    "5bdf3f90c5b601e5",
    "c4a6310cbdc392e5",
    "2d6d2288b5d123e5",
    "96341404addeb4e5",
    "fefb0580a5ec45e5",
    "67c1f6fc9df9d6e5",
    "d088e878960767e5",
    "394fd9f48e14f8e5",
    "a216cb70862289e5",
    "addbcec7e301ae5",
    "73a4ae68763dabe5",
    "dc6b9fe46e4b3ce5",
    "453291606658cde5",
    "adf982dc5e665ee5",
    "16c074585673efe5",
    "7f8765d44e8180e5",
    "e84e5750468f11e5",
    "511548cc3e9ca2e5",
    "b9dc3a4836aa33e5",
    "22a32bc42eb7c4e5",
    "8b6a1d4026c555e5",
    "f4310ebc1ed2e6e5",
    "5cf8003816e077e5",
    "c5bef1b40eee08e5",
    "2e85e33006fb99e5",
    "974cd4abff092ae5",
    "13c627f716bbe5",
    "68dab7a3ef244ce5",
    "d1a1a91fe731dde5",
    "3a689a9bdf3f6ee5",
    "a32f8c17d74cffe5",
    "bf67d93cf5a90e5",
    "74bd6f0fc76821e5",
    "dd84608bbf75b2e5",
    "464b5207b78343e5",
    "af124383af90d4e5",
    "17d934ffa79e65e5",
    "80a0267b9fabf6e5",
    "e96717f797b987e5",
    "522e09738fc718e5",
    "baf4faef87d4a9e5",
    "23bbec6b7fe23ae5",
    "8c82dde777efcbe5",
    "f549cf636ffd5ce5",
    "5e10c0df680aede5",
    "c6d7b25b60187ee5",
    "2f9ea3d758260fe5",
    "986595535033a0e5",
    "12c86cf484131e5",
    "69f3784b404ec2e5",
    "d2ba69c7385c53e5",
    "3b815b433069e4e5",
    "a4484cbf287775e5",
    "d0f3e3b208506e5",
    "75d62fb7189297e5",
    "de9d213310a028e5",
    "476412af08adb9e5",
    "b02b042b00bb4ae5",
    "18f1f5a6f8c8dbe5",
    "81b8e722f0d66ce5",
    "ea7fd89ee8e3fde5",
    "5346ca1ae0f18ee5",
    "bc0dbb96d8ff1fe5",
    "24d4ad12d10cb0e5",
    "8d9b9e8ec91a41e5",
    "f662900ac127d2e5",
    "5f298186b93563e5",
    "c7f07302b142f4e5",
    "30b7647ea95085e5",
    "997e55faa15e16e5",
    "2454776996ba7e5",
    "6b0c38f2917938e5",
    "d3d32a6e8986c9e5",
    "3c9a1bea81945ae5",
    "a5610d6679a1ebe5",
    "e27fee271af7ce5",
    "76eef05e69bd0de5",
    "dfb5e1da61ca9ee5",
    "487cd35659d82fe5",
    "b143c4d251e5c0e5",
    "1a0ab64e49f351e5",
    "82d1a7ca4200e2e5",
    "eb9899463a0e73e5",
    "545f8ac2321c04e5",
    "bd267c3e2a2995e5",
    "25ed6dba223726e5",
    "8eb45f361a44b7e5",
    "f77b50b2125248e5",
    "6042422e0a5fd9e5",
    "c90933aa026d6ae5",
    "31d02525fa7afbe5",
    "9a9716a1f2888ce5",
    "35e081dea961de5",
    "6c24f999e2a3aee5",
    "d4ebeb15dab13fe5",
    "3db2dc91d2bed0e5",
    "a679ce0dcacc61e5",
    "f40bf89c2d9f2e5",
    "7807b105bae783e5",
    "e0cea281b2f514e5",
    "499593fdab02a5e5",
    "b25c8579a31036e5",
    "1b2376f59b1dc7e5",
    "83ea6871932b58e5",
    "ecb159ed8b38e9e5",
    "55784b6983467ae5",
    "be3f3ce57b540be5",
    "27062e6173619ce5",
    "8fcd1fdd6b6f2de5",
    "f8941159637cbee5",
    "615b02d55b8a4fe5",
    "ca21f4515397e0e5",
    "32e8e5cd4ba571e5",
    "9bafd74943b302e5",
    "476c8c53bc093e5",
    "6d3dba4133ce24e5",
    "d604abbd2bdbb5e5",
    "3ecb9d3923e946e5",
    "a7928eb51bf6d7e5",
    "10598031140468e5",
    "792071ad0c11f9e5",
    "e1e76329041f8ae5",
    "4aae54a4fc2d1be5",
    "b3754620f43aace5",
    "1c3c379cec483de5",
    "85032918e455cee5",
    "edca1a94dc635fe5",
    "56910c10d470f0e5",
    "bf57fd8ccc7e81e5",
    "281eef08c48c12e5",
    "90e5e084bc99a3e5",
    "f9acd200b4a734e5",
    "6273c37cacb4c5e5",
    "cb3ab4f8a4c256e5",
    "3401a6749ccfe7e5",
    "9cc897f094dd78e5",
    "58f896c8ceb09e5",
    "6e567ae884f89ae5",
    "d71d6c647d062be5",
    "3fe45de07513bce5",
    "a8ab4f5c6d214de5",
    "117240d8652edee5",
    "7a3932545d3c6fe5",
    "e30023d0554a00e5",
    "4bc7154c4d5791e5",
    "b48e06c8456522e5",
    "1d54f8443d72b3e5",
    "861be9c0358044e5",
    "eee2db3c2d8dd5e5",
    "57a9ccb8259b66e5",
    "c070be341da8f7e5",
    "2937afb015b688e5",
    "91fea12c0dc419e5",
    "fac592a805d1aae5",
    "638c8423fddf3be5",
    "cc53759ff5eccce5",
    "351a671bedfa5de5",
    "9de15897e607eee5",
    "6a84a13de157fe5",
    "6f6f3b8fd62310e5",
    "d8362d0bce30a1e5",
    "40fd1e87c63e32e5",
    "a9c41003be4bc3e5",
    "128b017fb65954e5",
    "7b51f2fbae66e5e5",
    "e418e477a67476e5",
    "4cdfd5f39e8207e5",
    "b5a6c76f968f98e5",
    "1e6db8eb8e9d29e5",
    "8734aa6786aabae5",
    "effb9be37eb84be5",
    "58c28d5f76c5dce5",
    "c1897edb6ed36de5",
    "2a50705766e0fee5",
    "931761d35eee8fe5",
    "fbde534f56fc20e5",
    "64a544cb4f09b1e5",
    "cd6c3647471742e5",
    "363327c33f24d3e5",
    "9efa193f373264e5",
    "7c10abb2f3ff5e5",
    "7087fc37274d86e5",
    "d94eedb31f5b17e5",
    "4215df2f1768a8e5",
    "aadcd0ab0f7639e5",
    "13a3c2270783cae5",
    "7c6ab3a2ff915be5",
    "e531a51ef79eece5",
    "4df8969aefac7de5",
    "b6bf8816e7ba0ee5",
    "1f867992dfc79fe5",
    "884d6b0ed7d530e5",
    "f1145c8acfe2c1e5",
    "59db4e06c7f052e5",
    "c2a23f82bffde3e5",
    "2b6930feb80b74e5",
    "9430227ab01905e5",
    "fcf713f6a82696e5",
    "65be0572a03427e5",
    "ce84f6ee9841b8e5",
    "374be86a904f49e5",
    "a012d9e6885cdae5",
    "8d9cb62806a6be5",
    "71a0bcde7877fce5",
    "da67ae5a70858de5",
    "432e9fd668931ee5",
    "abf5915260a0afe5",
    "14bc82ce58ae40e5",
    "7d83744a50bbd1e5",
    "e64a65c648c962e5",
    "4f11574240d6f3e5",
    "b7d848be38e484e5",
    "209f3a3a30f215e5",
    "89662bb628ffa6e5",
    "f22d1d32210d37e5",
    "5af40eae191ac8e5",
    "c3bb002a112859e5",
    "2c81f1a60935eae5",
    "9548e32201437be5",
    "fe0fd49df9510ce5",
    "66d6c619f15e9de5",
    "cf9db795e96c2ee5",
    "3864a911e179bfe5",
    "a12b9a8dd98750e5",
    "9f28c09d194e1e5",
    "72b97d85c9a272e5",
    "db806f01c1b003e5",
    "4447607db9bd94e5",
    "ad0e51f9b1cb25e5",
    "15d54375a9d8b6e5",
    "7e9c34f1a1e647e5",
    "e763266d99f3d8e5",
    "502a17e9920169e5",
    "b8f109658a0efae5",
    "21b7fae1821c8be5",
    "8a7eec5d7a2a1ce5",
    "f345ddd97237ade5",
    "5c0ccf556a453ee5",
    "c4d3c0d16252cfe5",
    "2d9ab24d5a6060e5",
    "9661a3c9526df1e5",
    "ff2895454a7b82e5",
    "67ef86c1428913e5",
    "d0b6783d3a96a4e5"
  )
}
